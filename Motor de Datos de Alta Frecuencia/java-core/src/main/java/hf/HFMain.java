package hf;

import java.util.Random;

public class HFMain {
    public static void main(String[] args) throws Exception {
        HFRustAdapter adapter = new HFRustAdapter();
        HFEngine engine = new HFEngine(1 << 14, Runtime.getRuntime().availableProcessors(), adapter);
        engine.start();
        Random r = new Random(42);
        long start = System.nanoTime();
        int total = 200000;
        int sent = 0;
        while (sent < total) {
            long ts = System.currentTimeMillis();
            String sym = sent % 2 == 0 ? "ABC" : "XYZ";
            double price = 50 + r.nextDouble() * 10;
            long qty = 1 + r.nextInt(1000);
            if (engine.offer(ts, sym, price, qty)) {
                sent++;
            } else {
                Thread.onSpinWait();
            }
        }
        while (engine.processedCount() < total) {
            Thread.sleep(10);
        }
        long end = System.nanoTime();
        double seconds = (end - start) / 1_000_000_000.0;
        double throughput = engine.processedCount() / seconds;
        System.out.println("{\"processed\":" + engine.processedCount() + ",\"throughput\":" + throughput + ",\"last_notional\":" + engine.lastNotional() + "}");
        engine.stop();
    }
}

