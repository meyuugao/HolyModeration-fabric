"""Telegram bot: notify on launch with per-fingerprint inline buttons + handle callbacks.

Design for "multiple fingerprints arrive at once": one message per launch, with a
keyboard row per fingerprint. Each row shows the source tag + hash prefix, plus
[+ ВЛ] [+ ЧС] (or [− ВЛ] / [− ЧС] if already listed).

callback_data is short ("wl|<launchId>|<idx>") — the full hwid is looked up server-side
from pending_launches[launchId]. This stays well under Telegram's 64-byte limit
even with long launch ids.
"""
import os
import sys
import secrets
import time
import threading
import logging
from pathlib import Path
try:
    import requests
except ImportError:
    requests = None  # allows logic-only testing without the http deps

# Make sibling store.py importable when this module is loaded as part of the package.
_pkg_root = str(Path(__file__).resolve().parent)
if _pkg_root not in sys.path:
    sys.path.insert(0, _pkg_root)

log = logging.getLogger("holymoderation.telegram")

BOT_TOKEN = os.getenv("TELEGRAM_BOT_TOKEN", "")
CHAT_ID = os.getenv("TELEGRAM_CHAT_ID", "")
ADMIN_TG_ID = str(os.getenv("ADMIN_TG_ID", "")).strip()
WEBHOOK_URL = os.getenv("WEBHOOK_URL", "")

API = f"https://api.telegram.org/bot{BOT_TOKEN}" if BOT_TOKEN else ""

# Secret token Telegram sends back in the X-Telegram-Bot-Api-Secret-Token header.
# If WEBHOOK_SECRET is set in env, the webhook route MUST reject requests without it.
WEBHOOK_SECRET = os.getenv("WEBHOOK_SECRET", "")

# Fingerprint source labels, indexed by collection order in the mod's HwidService.
SOURCE_LABELS = {
    0: "mb",   # motherboard SMBIOS UUID
    1: "bb",   # baseboard serial
    2: "cpu",  # processor id
    3: "disk", # disk serial
    4: "nic",  # nic mac
}

# Pending launches: launchId -> {"username": str, "hwids": [str], "ts": float, "msg": {...}}.
# In-memory only; old buttons answer "устарело" after a restart — buttons are ephemeral.
_pending = {}
_pending_lock = threading.Lock()
_counter = 0


def _next_launch_id():
    global _counter
    with _pending_lock:
        _counter += 1
        return _counter


def _register(username, hwids, message):
    lid = _next_launch_id()
    with _pending_lock:
        _purge_old_locked()
        _pending[lid] = {
            "username": username,
            "hwids": list(hwids),
            "ts": time.time(),
            "message": message,  # chat_id + message_id for editMessageReplyMarkup
        }
    return lid


def _purge_old_locked():
    # drop entries older than 1 hour to bound memory
    cutoff = time.time() - 3600
    stale = [k for k, v in _pending.items() if v["ts"] < cutoff]
    for k in stale:
        del _pending[k]


def _get_launch(lid):
    with _pending_lock:
        entry = _pending.get(lid)
        if entry is None:
            return None
        return dict(entry)


def _tg(method, payload):
    if not BOT_TOKEN or requests is None:
        return None
    try:
        r = requests.post(f"{API}/{method}", json=payload, timeout=10)
        return r.json()
    except Exception as e:
        log.warning("telegram %s failed: %s", method, e)
        return None


def _short(hwid):
    return hwid[:8] if hwid else "????????"


def _build_keyboard(launch_id, hwids):
    """One row per fingerprint with +/- whitelist/blacklist toggle buttons."""
    import store

    rows = []
    for idx, h in enumerate(hwids):
        tag = SOURCE_LABELS.get(idx, f"fp{idx}")
        label = f"[{tag}] {_short(h)}"
        in_wl = store.is_in_whitelist(h)
        in_bl = store.is_in_blacklist(h)

        wl_btn = {
            "text": "− ВЛ" if in_wl else "+ ВЛ",
            "callback_data": f"wl|{launch_id}|{idx}",
        }
        bl_btn = {
            "text": "− ЧС" if in_bl else "+ ЧС",
            "callback_data": f"bl|{launch_id}|{idx}",
        }
        rows.append([
            {"text": label, "callback_data": f"info|{launch_id}|{idx}"},
            wl_btn,
            bl_btn,
        ])
    return {"inline_keyboard": rows}


def notify_launch(username, hwids):
    """Send one launch message with per-fingerprint buttons. Returns None on failure."""
    if not (BOT_TOKEN and CHAT_ID):
        log.warning("notify_launch: BOT_TOKEN or CHAT_ID not set")
        return None

    when = time.strftime("%Y-%m-%d %H:%M:%S")
    text = (
        f"🎮 Заход на сервер\n"
        f"Ник: <b>{_escape(username)}</b>\n"
        f"⏱ {when}\n\n"
        f"🔐 Fingerprints ({len(hwids)}): нажимайте кнопки справа, чтобы управлять."
    )
    payload = {
        "chat_id": CHAT_ID,
        "text": text,
        "parse_mode": "HTML",
        "reply_markup": _build_keyboard(_peek_next_id(), hwids),
    }
    # NOTE: launch_id must match the keyboard. Reserve the id first, then send.
    launch_id = _next_launch_id()
    payload["reply_markup"] = _build_keyboard(launch_id, hwids)
    resp = _tg("sendMessage", payload)
    if not resp or not resp.get("ok"):
        log.warning("sendMessage failed: %s", resp)
        return None

    sent = resp.get("result") or {}
    _register(username, hwids, {
        "chat_id": sent.get("chat", {}).get("id"),
        "message_id": sent.get("message_id"),
    })
    return launch_id


def _peek_next_id():
    """Best-effort preview of the next launch id (used to keep keyboard consistent)."""
    with _pending_lock:
        return _counter + 1


def _escape(s):
    return (s or "").replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")


def _answer(callback_id, text, show_alert=False):
    if not callback_id:
        return
    _tg("answerCallbackQuery", {
        "callback_query_id": callback_id,
        "text": text[:200],
        "show_alert": show_alert,
    })


def _edit_markup(message_ref, launch_id, hwids):
    """Refresh the keyboard to reflect the new whitelist/blacklist state. Failures are ignored."""
    if not message_ref:
        return
    chat_id = message_ref.get("chat_id")
    message_id = message_ref.get("message_id")
    if chat_id is None or message_id is None:
        return
    _tg("editMessageReplyMarkup", {
        "chat_id": chat_id,
        "message_id": message_id,
        "reply_markup": _build_keyboard(launch_id, hwids),
    })


def _describe_state(hwid):
    """Human-readable WL/ЧС state for a fingerprint."""
    import store
    owner_wl = store.whitelist_owner(hwid)
    if owner_wl:
        return "в whitelist: " + owner_wl
    owner_bl = store.blacklist_owner(hwid)
    if owner_bl:
        return "в blacklist: " + owner_bl
    return "нет в списках"


def handle_callback(update):
    """Process an incoming Telegram Update with a callback_query."""
    cb = update.get("callback_query")
    if not cb:
        return {"status": "ignored"}

    cb_id = cb.get("id")
    frm = cb.get("from", {}) or {}
    user_id = str(frm.get("id", ""))

    # Always answer the callback first-thing in error branches so the button stops spinning.
    if ADMIN_TG_ID and user_id != ADMIN_TG_ID:
        _answer(cb_id, "⛔ Нет прав", show_alert=True)
        return {"status": "forbidden"}

    data = cb.get("data", "") or ""
    message = cb.get("message") or {}

    parts = data.split("|")
    if len(parts) != 3:
        _answer(cb_id, "Некорректный запрос")
        return {"status": "bad_data"}

    action, launch_id_str, idx_str = parts
    try:
        launch_id = int(launch_id_str)
        idx = int(idx_str)
    except ValueError:
        _answer(cb_id, "Некорректный запрос")
        return {"status": "bad_data"}

    entry = _get_launch(launch_id)
    if entry is None:
        _answer(cb_id, "⏰ Запрос устарел (сервер перезапущен)", show_alert=True)
        return {"status": "expired"}

    hwids = entry["hwids"]
    username = entry["username"]
    message_ref = entry.get("message")
    if idx < 0 or idx >= len(hwids):
        _answer(cb_id, "Fingerprint не найден")
        return {"status": "bad_index"}

    hwid = hwids[idx]
    short = _short(hwid)

    import store

    if action == "wl":
        if store.is_in_whitelist(hwid):
            store.remove_whitelist(hwid)
            _answer(cb_id, f"🗑 Удалён из ВЛ: {short}")
        else:
            # adding to whitelist removes from blacklist (mutually exclusive)
            store.remove_blacklist(hwid)
            store.add_whitelist(hwid, username)
            _answer(cb_id, f"✅ Добавлен в ВЛ: {short}")
        _edit_markup(message_ref, launch_id, hwids)
        return {"status": "ok"}

    if action == "bl":
        if store.is_in_blacklist(hwid):
            store.remove_blacklist(hwid)
            _answer(cb_id, f"🗑 Удалён из ЧС: {short}")
        else:
            store.remove_whitelist(hwid)
            store.add_blacklist(hwid, username)
            _answer(cb_id, f"⛔ Добавлен в ЧС: {short}")
        _edit_markup(message_ref, launch_id, hwids)
        return {"status": "ok"}

    if action == "info":
        state = _describe_state(hwid)
        _answer(cb_id, f"{short}…\n{state}\nник захода: {username}", show_alert=True)
        return {"status": "ok"}

    _answer(cb_id, "Неизвестное действие")
    return {"status": "unknown_action"}


def set_webhook(url):
    if not (BOT_TOKEN and url):
        return {"error": "BOT_TOKEN or url missing"}
    if requests is None:
        return {"error": "requests not available"}
    params = {"url": url, "allowed_updates": '["callback_query", "message"]'}
    if WEBHOOK_SECRET:
        params["secret_token"] = WEBHOOK_SECRET
    try:
        r = requests.get(f"{API}/setWebhook", params=params, timeout=10)
        return r.json()
    except Exception as e:
        return {"error": str(e)}


def delete_webhook():
    if not BOT_TOKEN:
        return {"error": "BOT_TOKEN missing"}
    if requests is None:
        return {"error": "requests not available"}
    try:
        r = requests.get(f"{API}/deleteWebhook", timeout=10)
        return r.json()
    except Exception as e:
        return {"error": str(e)}


def gen_secret():
    """Generate a fresh secret token for the webhook (for /tg/setup guidance)."""
    return secrets.token_urlsafe(32)


def get_webhook_info():
    """Return Telegram's own view of the webhook state (url, errors, pending updates)."""
    if requests is None:
        return {"error": "requests not available"}
    try:
        r = requests.get(f"{API}/getWebhookInfo", timeout=10)
        return r.json()
    except Exception as e:
        return {"error": str(e)}


def pending_summary():
    """A small snapshot of pending launches for diagnostics."""
    with _pending_lock:
        return {
            "count": len(_pending),
            "launches": [
                {"launch_id": k, "username": v["username"],
                 "hwids": v["hwids"], "age_sec": int(time.time() - v["ts"])}
                for k, v in _pending.items()
            ],
        }
