import sys, base64, json

def handle(req):
    if req.get("op") == "echo":
        msg = req.get("msg","")
        return {"ok": True, "result": {"msg": msg[::-1]}, "mode": "PYTHON"}
    if req.get("op") == "sum":
        vals = req.get("values", [])
        s = sum(vals)
        return {"ok": True, "result": {"sum": s}, "mode": "PYTHON"}
    return {"ok": False, "result": {"error": "invalid_request"}, "mode": "PYTHON"}

if __name__ == "__main__":
    b64 = sys.argv[1] if len(sys.argv) > 1 else ""
    try:
        raw = base64.b64decode(b64.encode("ascii")).decode("utf-8")
        req = json.loads(raw)
        res = handle(req)
        print(json.dumps(res))
    except Exception as e:
        print(json.dumps({"ok": False, "result": {"error": "python_exception"}, "mode": "PYTHON"}))

