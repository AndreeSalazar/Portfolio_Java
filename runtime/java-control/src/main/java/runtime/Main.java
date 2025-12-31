package runtime;

import java.util.Arrays;
import java.util.concurrent.Future;

public class Main {
    public static void main(String[] args) throws Exception {
        NativeRuntime nr = new NativeRuntime();
        RuntimeEngine engine = new RuntimeEngine(nr, Runtime.getRuntime().availableProcessors());

        EngineTask sum = new SumTask(new double[]{1.0, 2.5, 3.5});
        String sumOut = engine.runSync(sum);
        System.out.println(sumOut);

        EngineTask sim = new SimTask(100000, 42);
        Future<String> simFuture = engine.submit(sim);
        System.out.println(simFuture.get());

        engine.shutdown();
    }

    static class SumTask implements EngineTask {
        final double[] values;
        SumTask(double[] values) { this.values = values; }
        public String toJsonRequest() {
            StringBuilder sb = new StringBuilder();
            sb.append("{\"op\":\"sum\",\"values\":[");
            for (int i = 0; i < values.length; i++) {
                if (i > 0) sb.append(",");
                sb.append(values[i]);
            }
            sb.append("]}");
            return sb.toString();
        }
    }

    static class SimTask implements EngineTask {
        final long steps;
        final long seed;
        SimTask(long steps, long seed) { this.steps = steps; this.seed = seed; }
        public String toJsonRequest() {
            return "{\"op\":\"simulate\",\"steps\":" + steps + ",\"seed\":" + seed + "}";
        }
    }
}

