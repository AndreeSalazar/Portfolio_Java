import time
import random
import statistics

def run_hft_benchmark():
    print("Running HFT Latency Benchmark...")
    latencies = []
    # Simulate 100k operations
    for _ in range(100000):
        start = time.time_ns()
        # Simulate tiny JNI call overhead
        _ = 1 + 1
        end = time.time_ns()
        latencies.append((end - start) / 1000.0) # Microseconds

    p50 = statistics.median(latencies)
    p99 = statistics.quantiles(latencies, n=100)[98]
    print(f"HFT Result: P50={p50:.2f}us, P99={p99:.2f}us")
    return {"p50": p50, "p99": p99}

if __name__ == "__main__":
    run_hft_benchmark()
