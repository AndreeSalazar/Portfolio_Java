package runtime;

import java.util.concurrent.*;

public class RuntimeEngine {
    private final NativeRuntime nativeRuntime;
    private final ExecutorService pool;

    public RuntimeEngine(NativeRuntime nativeRuntime, int threads) {
        this.nativeRuntime = nativeRuntime;
        this.pool = Executors.newFixedThreadPool(Math.max(1, threads));
    }

    public Future<String> submit(EngineTask task) {
        return pool.submit(() -> nativeRuntime.execute(task.toJsonRequest()));
    }

    public String runSync(EngineTask task) {
        return nativeRuntime.execute(task.toJsonRequest());
    }

    public void shutdown() {
        pool.shutdownNow();
        nativeRuntime.shutdown();
    }
}

