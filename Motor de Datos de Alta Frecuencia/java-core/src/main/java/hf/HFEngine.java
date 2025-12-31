package hf;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class HFEngine {
    static class Event {
        long ts;
        String symbol;
        double price;
        long qty;
        String csv;
    }
    private final int capacity;
    private final Event[] ring;
    private final AtomicInteger head = new AtomicInteger(0);
    private final AtomicInteger tail = new AtomicInteger(0);
    private final ExecutorService pool;
    private final HFRustAdapter adapter;
    private volatile boolean running = false;
    private long processed = 0;
    private double lastNotional = 0.0;

    public HFEngine(int capacity, int threads, HFRustAdapter adapter) {
        this.capacity = capacity;
        this.ring = new Event[capacity];
        for (int i=0;i<capacity;i++) ring[i] = new Event();
        this.pool = Executors.newFixedThreadPool(Math.max(1, threads));
        this.adapter = adapter;
    }

    public boolean offer(long ts, String symbol, double price, long qty) {
        int h = head.get();
        int t = tail.get();
        int next = (h + 1) % capacity;
        if (next == t) return false;
        Event e = ring[h];
        e.ts = ts; e.symbol = symbol; e.price = price; e.qty = qty;
        e.csv = ts + "," + symbol + "," + price + "," + qty;
        head.set(next);
        return true;
    }

    public void start() {
        running = true;
        pool.submit(this::consumeLoop);
    }

    public void stop() {
        running = false;
        pool.shutdownNow();
        adapter.shutdown();
    }

    private void consumeLoop() {
        while (running) {
            int t = tail.get();
            if (t == head.get()) {
                Thread.onSpinWait();
                continue;
            }
            Event e = ring[t];
            tail.set((t + 1) % capacity);
            String out = adapter.parseCsv(e.csv);
            double notional = extractDouble(out, "notional");
            lastNotional = notional;
            processed++;
        }
    }

    public long processedCount() { return processed; }
    public double lastNotional() { return lastNotional; }

    private double extractDouble(String s, String key) {
        String k = "\"" + key + "\"";
        int i = s.indexOf(k);
        int start = s.indexOf(':', i);
        int j = start + 1;
        while (j < s.length()) {
            char c = s.charAt(j);
            if ((c >= '0' && c <= '9') || c == '-' || c == '+' || c == '.' || c == 'e' || c == 'E') j++;
            else break;
        }
        try {
            return Double.parseDouble(s.substring(start + 1, j).trim());
        } catch (Exception ex) {
            return 0.0;
        }
    }
}

