"""HolyModeration backend (Flask WSGI app exposed as `application`).

Endpoints:
  GET  /                  healthcheck
  GET  /whitelist         {hwid: nickname}
  GET  /blacklist         {hwid: nickname}
  POST /api/launch        body {username, hwids:[...]} -> notifies Telegram with buttons
  POST /tg/webhook        Telegram update (callback buttons)
  GET  /tg/setup          one-shot webhook registration (admin only, via ADMIN_TG_ID query or env)
  GET  /viewer            Viewer.jar download
"""
import os
import sys
import logging

from flask import Flask, request, jsonify, send_file
from pathlib import Path

# Ensure sibling modules (store.py, telegram.py) are importable regardless of how
# alwaysdata/uWSGI mounts this package (top-level vs `www:application`).
BASE_DIR = Path(__file__).resolve().parent
_pkg_root = str(BASE_DIR)
if _pkg_root not in sys.path:
    sys.path.insert(0, _pkg_root)

# Relative import is the canonical form for a package; falls back to the absolute
# import (works after sys.path tweak above) if loaded as a top-level module.
try:
    from . import store, telegram
except ImportError:
    import store
    import telegram

app = Flask(__name__)
app.logger.setLevel(logging.INFO)

ADMIN_TG_ID = os.getenv("ADMIN_TG_ID", "")


@app.route("/")
def root():
    return jsonify({"status": "alive"})


@app.route("/whitelist", methods=["GET"])
def get_whitelist():
    return jsonify(store.list_whitelist())


@app.route("/blacklist", methods=["GET"])
def get_blacklist():
    return jsonify(store.list_blacklist())


@app.route("/api/launch", methods=["POST"])
def api_launch():
    data = request.get_json(silent=True) or {}
    username = data.get("username")
    hwids = data.get("hwids")

    if not username or not isinstance(hwids, list):
        return jsonify({"error": "Expected JSON body {username, hwids:[...]}"}), 400

    # de-dup preserving order
    seen = set()
    clean = []
    for h in hwids:
        if isinstance(h, str) and h and h not in seen:
            seen.add(h)
            clean.append(h)

    try:
        launch_id = telegram.notify_launch(username, clean)
    except Exception as e:
        app.logger.exception("telegram.notify_launch failed: %s", e)
        launch_id = None

    return jsonify({"status": "ok", "fingerprints": len(clean), "launch_id": launch_id})


@app.route("/tg/webhook", methods=["POST"])
def tg_webhook():
    # Optional shared-secret check: if WEBHOOK_SECRET is configured, Telegram echoes it
    # back in X-Telegram-Bot-Api-Secret-Token. Reject anyone else.
    secret = os.getenv("WEBHOOK_SECRET", "").strip()
    if secret:
        incoming = request.headers.get("X-Telegram-Bot-Api-Secret-Token", "")
        if incoming != secret:
            app.logger.warning("webhook: bad secret token, rejecting")
            return jsonify({"error": "bad token"}), 401

    update = request.get_json(silent=True) or {}
    app.logger.info("webhook update keys: %s", list(update.keys()))
    try:
        result = telegram.handle_callback(update)
    except Exception as e:
        app.logger.exception("telegram.handle_callback failed: %s", e)
        result = {"status": "error"}
    # Telegram expects 200 even on logical failures
    return jsonify(result), 200


def _is_admin():
    """Admin check via query param ?admin=<ADMIN_TG_ID> (for browser setup)."""
    q = request.args.get("admin", "")
    return bool(ADMIN_TG_ID) and q == str(ADMIN_TG_ID)


@app.route("/tg/setup", methods=["GET"])
def tg_setup():
    if not _is_admin():
        return jsonify({"error": "forbidden"}), 403
    url = os.getenv("WEBHOOK_URL", "").strip()
    if not url:
        return jsonify({"error": "WEBHOOK_URL env not set"}), 500
    return jsonify(telegram.set_webhook(url))


@app.route("/tg/secret", methods=["GET"])
def tg_secret():
    """Generate a fresh webhook secret to paste into the WEBHOOK_SECRET env var."""
    if not _is_admin():
        return jsonify({"error": "forbidden"}), 403
    return jsonify({"secret": telegram.gen_secret(), "note": "Set as WEBHOOK_SECRET env, then call /tg/setup?admin=<ADMIN_TG_ID>"})


@app.route("/tg/unset", methods=["GET"])
def tg_unset():
    if not _is_admin():
        return jsonify({"error": "forbidden"}), 403
    return jsonify(telegram.delete_webhook())


@app.route("/tg/status", methods=["GET"])
def tg_status():
    """Telegram's own view of the webhook + current pending launches. Admin only."""
    if not _is_admin():
        return jsonify({"error": "forbidden"}), 403
    return jsonify({"webhook": telegram.get_webhook_info(), "pending": telegram.pending_summary()})


@app.route("/pending", methods=["GET"])
def pending():
    """Current pending launches (admin only)."""
    if not _is_admin():
        return jsonify({"error": "forbidden"}), 403
    return jsonify(telegram.pending_summary())


@app.route("/viewer", methods=["GET"])
def get_viewer():
    viewer_path = BASE_DIR / "viewer" / "Viewer.jar"
    if not viewer_path.exists():
        return jsonify({"error": "Viewer.jar not found"}), 404
    return send_file(
        viewer_path,
        mimetype="application/java-archive",
        as_attachment=True,
        download_name="Viewer.jar",
    )


application = app
