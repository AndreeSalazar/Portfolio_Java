import sys, socket, base64, json, time

def send(req):
    s = socket.socket()
    s.connect(("127.0.0.1", 9092))
    s.send((req + "\n").encode("utf-8"))
    out = s.recv(2**20).decode("utf-8").splitlines()[0]
    s.close()
    return out

def compress_file(path):
    with open(path, "rb") as f:
        data = f.read()
    b64 = base64.b64encode(data).decode("ascii")
    req = json.dumps({"op":"compress","data_b64":b64,"algo":"gzip"})
    t0 = time.time()
    out = send(req)
    t1 = time.time()
    res = json.loads(out)
    print(json.dumps({"mode":res.get("mode"),"ok":res.get("ok"),"ms":int((t1-t0)*1000),"size_before":res["result"]["size_before"],"size_after":res["result"]["size_after"]}))
    ob64 = res["result"]["data_b64"]
    with open(path + ".gz.b64", "w") as g:
        g.write(ob64)

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("usage: build.py <asset_file>")
        sys.exit(1)
    compress_file(sys.argv[1])

