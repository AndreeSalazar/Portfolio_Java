import json, random

def gen(n=1000, seed=123):
    random.seed(seed)
    xs = [random.random()*10.0 for _ in range(n)]
    ys = [2.0*x + 3.0 + (random.random()-0.5)*0.2 for x in xs]
    return xs, ys

def train(xs, ys):
    n = len(xs)
    mx = sum(xs)/n
    my = sum(ys)/n
    cov = sum((xs[i]-mx)*(ys[i]-my) for i in range(n))
    var = sum((xs[i]-mx)*(xs[i]-mx) for i in range(n))
    w = cov/var if var != 0.0 else 0.0
    b = my - w*mx
    return w, b

if __name__ == "__main__":
    xs, ys = gen()
    w, b = train(xs, ys)
    print(json.dumps({"ok": True, "w": w, "b": b}))
