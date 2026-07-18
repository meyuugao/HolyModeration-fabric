"""Thread-safe store for whitelist/blacklist as {hwid: nickname}.

Both files live next to this module (BASE_DIR / <name>.json). Writes are atomic
(write temp + os.replace) so a crash mid-write never leaves a truncated file.
"""
import os
import json
import threading
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent

_LOCK = threading.Lock()


def _path(name):
    return BASE_DIR / f"{name}.json"


def _read(name):
    p = _path(name)
    if not p.exists():
        return {}
    try:
        with open(p, "r", encoding="utf-8") as f:
            data = json.load(f)
            return data if isinstance(data, dict) else {}
    except (json.JSONDecodeError, OSError):
        return {}


def _write(name, data):
    p = _path(name)
    tmp = p.with_suffix(p.suffix + ".tmp")
    with open(tmp, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2, sort_keys=True)
    os.replace(tmp, p)


def list_whitelist():
    with _LOCK:
        return _read("whitelist")


def list_blacklist():
    with _LOCK:
        return _read("blacklist")


def _add(list_name, hwid, nickname):
    if not hwid:
        return False
    with _LOCK:
        data = _read(list_name)
        if data.get(hwid) == nickname:
            return False
        data[hwid] = nickname
        _write(list_name, data)
        return True


def _remove(list_name, hwid):
    if not hwid:
        return False
    with _LOCK:
        data = _read(list_name)
        if hwid not in data:
            return False
        del data[hwid]
        _write(list_name, data)
        return True


def add_whitelist(hwid, nickname):
    return _add("whitelist", hwid, nickname)


def remove_whitelist(hwid):
    return _remove("whitelist", hwid)


def add_blacklist(hwid, nickname):
    return _add("blacklist", hwid, nickname)


def remove_blacklist(hwid):
    return _remove("blacklist", hwid)


def is_in_whitelist(hwid):
    with _LOCK:
        return hwid in _read("whitelist")


def is_in_blacklist(hwid):
    with _LOCK:
        return hwid in _read("blacklist")


def whitelist_owner(hwid):
    with _LOCK:
        return _read("whitelist").get(hwid)


def blacklist_owner(hwid):
    with _LOCK:
        return _read("blacklist").get(hwid)
