package os;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Base64;

public class Kernel {
    public enum State { NEW, READY, RUNNING, WAITING, TERMINATED }

    public interface OSProcess {
        String pid();
        State state();
        void onStart(Kernel k);
        void onTick(Kernel k);
        void onEvent(Event e, Kernel k);
    }

    public static class Event {
        public final String type;
        public final String data;
        public Event(String type, String data){ this.type=type; this.data=data; }
    }

    private final OSRustAdapter adapter = new OSRustAdapter();
    private final List<OSProcess> processes = new ArrayList<>();
    private final ArrayDeque<Event> events = new ArrayDeque<>();
    private int tick = 0;

    public void register(OSProcess p){
        processes.add(p);
    }

    public void start(){
        for (OSProcess p : processes) p.onStart(this);
    }

    public void pushEvent(String type, String data){
        events.add(new Event(type, data));
    }

    public void step(){
        tick++;
        while (!events.isEmpty()){
            Event e = events.poll();
            for (OSProcess p : processes) p.onEvent(e, this);
        }
        for (OSProcess p : processes){
            p.onTick(this);
        }
    }

    public int tick(){ return tick; }

    public String memAlloc(int size){
        String req = "{\"op\":\"alloc\",\"size\":" + size + "}";
        String out = adapter.execute(req);
        int id = (int) extractNumber(out, "id");
        return String.valueOf(id);
    }

    public void memWrite(String id, byte[] data){
        String b64 = Base64.getEncoder().encodeToString(data);
        String req = "{\"op\":\"write\",\"id\":" + id + ",\"data\":\"" + b64 + "\"}";
        adapter.execute(req);
    }

    public byte[] memRead(String id){
        String req = "{\"op\":\"read\",\"id\":" + id + "}";
        String out = adapter.execute(req);
        String b64 = extractString(out, "data");
        if (b64 == null) return new byte[0];
        return Base64.getDecoder().decode(b64);
    }

    public void memFree(String id){
        String req = "{\"op\":\"free\",\"id\":" + id + "}";
        adapter.execute(req);
    }

    public String io(String kind, String data){
        String req = "{\"op\":\"io\",\"kind\":\"" + kind + "\",\"data\":\"" + data + "\"}";
        return adapter.execute(req);
    }

    public void shutdown(){
        adapter.shutdown();
    }

    static double extractNumber(String s, String key){
        String k = "\"" + key + "\"";
        int i = s.indexOf(k);
        int start = s.indexOf(':', i);
        int j = start + 1;
        while (j < s.length()) {
            char c = s.charAt(j);
            if ((c >= '0' && c <= '9') || c == '-' || c == '+' || c == '.' || c == 'e' || c == 'E') j++;
            else break;
        }
        try { return Double.parseDouble(s.substring(start+1, j).trim()); } catch(Exception e){ return 0.0; }
    }

    static String extractString(String s, String key){
        String k = "\"" + key + "\"";
        int i = s.indexOf(k);
        int start = s.indexOf(':', i);
        int q1 = s.indexOf('"', start+1);
        int q2 = s.indexOf('"', q1+1);
        if (q1 < 0 || q2 < 0) return null;
        return s.substring(q1+1, q2);
    }

    public static class ComputeProc implements OSProcess {
        private final String id;
        private State st = State.NEW;
        private String memId;
        private double a = 2.0;
        private double b = 3.0;
        private int x = 0;
        public ComputeProc(String id){ this.id=id; }
        public String pid(){ return id; }
        public State state(){ return st; }
        public void onStart(Kernel k){
            memId = k.memAlloc(64);
            st = State.READY;
        }
        public void onTick(Kernel k){
            st = State.RUNNING;
            x++;
            double y = a * x + b;
            String s = "{\"pid\":\"" + id + "\",\"tick\":" + k.tick() + ",\"x\":" + x + ",\"y\":" + y + "}";
            k.memWrite(memId, s.getBytes(StandardCharsets.UTF_8));
            if (x % 3 == 0) k.pushEvent("compute_done", id);
            st = State.READY;
        }
        public void onEvent(Event e, Kernel k){
            if (e.type.equals("io_ack")) st = State.READY;
        }
    }

    public static class IOProc implements OSProcess {
        private final String id;
        private State st = State.NEW;
        private int counter = 0;
        public IOProc(String id){ this.id=id; }
        public String pid(){ return id; }
        public State state(){ return st; }
        public void onStart(Kernel k){
            st = State.READY;
        }
        public void onTick(Kernel k){
            st = State.RUNNING;
            counter++;
            String payload = Base64.getEncoder().encodeToString(("io:" + id + ":" + k.tick()).getBytes(StandardCharsets.UTF_8));
            String out = k.io("disk_write", payload);
            k.pushEvent("io_ack", id);
            st = State.WAITING;
            st = State.READY;
        }
        public void onEvent(Event e, Kernel k){
            if (e.type.equals("compute_done")) st = State.READY;
        }
    }
}
