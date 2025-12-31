package sim;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class NativeSim {
    public enum Mode { JNI, IPC, JAVA }
    private Mode mode = Mode.JAVA;
    private Process ipcProcess;

    public NativeSim() {
        init();
    }

    private native String scExecuteJNI(String requestJson);

    private void init() {
        if (tryLoadNative()) {
            mode = Mode.JNI;
            return;
        }
        if (tryStartIpc()) {
            mode = Mode.IPC;
            return;
        }
        mode = Mode.JAVA;
    }

    private boolean tryLoadNative() {
        String explicit = System.getenv("SIM_NATIVE_LIB");
        try {
            if (explicit != null && !explicit.isBlank()) {
                System.load(explicit);
                return true;
            }
            System.loadLibrary("sim_core");
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private boolean tryStartIpc() {
        try {
            String bin = System.getenv("SIM_IPC_BIN");
            if (bin == null || bin.isBlank()) {
                Path p = Path.of("sim-framework", "rust-core", "target", "debug", "sim-ipc.exe");
                if (!Files.exists(p)) return false;
                bin = p.toString();
            }
            ProcessBuilder pb = new ProcessBuilder(bin);
            pb.redirectErrorStream(true);
            ipcProcess = pb.start();
            Thread.sleep(300);
            try (Socket s = new Socket()) {
                s.connect(new InetSocketAddress("127.0.0.1", 9091), 300);
                return true;
            } catch (IOException e) {
                return false;
            }
        } catch (Throwable t) {
            return false;
        }
    }

    public String execute(String requestJson) {
        switch (mode) {
            case JNI:
                return scExecuteJNI(requestJson);
            case IPC:
                return executeIpc(requestJson);
            default:
                return executeJava(requestJson);
        }
    }

    private String executeIpc(String requestJson) {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress("127.0.0.1", 9091), 500);
            OutputStream os = s.getOutputStream();
            InputStream is = s.getInputStream();
            os.write((requestJson + "\n").getBytes(StandardCharsets.UTF_8));
            os.flush();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String out = br.readLine();
            if (out == null) return "{\"ok\":false,\"result\":{\"error\":\"ipc_no_response\"},\"mode\":\"IPC\"}";
            return out;
        } catch (IOException e) {
            return "{\"ok\":false,\"result\":{\"error\":\"ipc_error\"},\"mode\":\"IPC\"}";
        }
    }

    private String executeJava(String requestJson) {
        return JavaSim.process(requestJson);
    }

    public void shutdown() {
        if (ipcProcess != null) {
            ipcProcess.destroy();
        }
    }

    static class JavaSim {
        static String process(String input) {
            try {
                var p = new SimpleJson(input);
                String op = p.getString("op");
                if ("metrics".equals(op)) {
                    World w = p.getWorld("world");
                    double k = w.kineticEnergy();
                    return "{\"ok\":true,\"result\":{\"bodies\":" + w.bodies.length + ",\"kinetic_energy\":" + k + "},\"mode\":\"JAVA\"}";
                }
                if ("step".equals(op)) {
                    World w = p.getWorld("world");
                    double dt = p.getDouble("dt");
                    String events = w.step(dt);
                    return "{\"ok\":true,\"result\":{\"world\":" + w.toJson() + ",\"events\":" + events + "},\"mode\":\"JAVA\"}";
                }
                return "{\"ok\":false,\"result\":{\"error\":\"invalid_request\"},\"mode\":\"JAVA\"}";
            } catch (Exception e) {
                return "{\"ok\":false,\"result\":{\"error\":\"invalid_json\"},\"mode\":\"JAVA\"}";
            }
        }
    }

    static class World {
        final double width;
        final double height;
        final Body[] bodies;
        World(double width, double height, Body[] bodies) {
            this.width = width; this.height = height; this.bodies = bodies;
        }
        String step(double dt) {
            StringBuilder ev = new StringBuilder();
            ev.append("[");
            boolean first = true;
            for (Body b : bodies) {
                b.x += b.vx * dt;
                b.y += b.vy * dt;
                if (b.x - b.r < 0) { b.x = b.r; b.vx = -b.vx; first = appendEvent(ev, first, "{\"type\":\"boundary_bounce\",\"side\":\"left\"}"); }
                if (b.x + b.r > width) { b.x = width - b.r; b.vx = -b.vx; first = appendEvent(ev, first, "{\"type\":\"boundary_bounce\",\"side\":\"right\"}"); }
                if (b.y - b.r < 0) { b.y = b.r; b.vy = -b.vy; first = appendEvent(ev, first, "{\"type\":\"boundary_bounce\",\"side\":\"top\"}"); }
                if (b.y + b.r > height) { b.y = height - b.r; b.vy = -b.vy; first = appendEvent(ev, first, "{\"type\":\"boundary_bounce\",\"side\":\"bottom\"}"); }
            }
            for (int i = 0; i < bodies.length; i++) {
                for (int j = i + 1; j < bodies.length; j++) {
                    Body a = bodies[i], b = bodies[j];
                    double dx = b.x - a.x, dy = b.y - a.y;
                    double dist2 = dx*dx + dy*dy;
                    double rsum = a.r + b.r;
                    if (dist2 <= rsum*rsum) {
                        resolveCollision(a, b);
                        first = appendEvent(ev, first, "{\"type\":\"collision\",\"i\":"+i+",\"j\":"+j+"}");
                    }
                }
            }
            ev.append("]");
            return ev.toString();
        }
        static boolean appendEvent(StringBuilder ev, boolean first, String e) {
            if (!first) ev.append(",");
            ev.append(e);
            return false;
        }
        static void resolveCollision(Body a, Body b) {
            double nx = b.x - a.x, ny = b.y - a.y;
            double dist = Math.sqrt(nx*nx + ny*ny);
            if (dist == 0.0) return;
            nx /= dist; ny /= dist;
            double dvx = b.vx - a.vx, dvy = b.vy - a.vy;
            double rel = dvx*nx + dvy*ny;
            if (rel > 0.0) return;
            double m1 = a.mass, m2 = b.mass;
            double e = 1.0;
            double j = -(1.0+e) * rel / (1.0/m1 + 1.0/m2);
            double ix = j*nx, iy = j*ny;
            a.vx -= ix/m1; a.vy -= iy/m1;
            b.vx += ix/m2; b.vy += iy/m2;
        }
        double kineticEnergy() {
            double k = 0.0;
            for (Body b : bodies) {
                double v2 = b.vx*b.vx + b.vy*b.vy;
                k += 0.5*b.mass*v2;
            }
            return k;
        }
        String toJson() {
            StringBuilder sb = new StringBuilder();
            sb.append("{\"width\":").append(width).append(",\"height\":").append(height).append(",\"bodies\":[");
            for (int i = 0; i < bodies.length; i++) {
                if (i > 0) sb.append(",");
                sb.append(bodies[i].toJson());
            }
            sb.append("]}");
            return sb.toString();
        }
    }

    static class Body {
        double x,y,vx,vy,r,mass;
        Body(double x,double y,double vx,double vy,double r,double mass){
            this.x=x;this.y=y;this.vx=vx;this.vy=vy;this.r=r;this.mass=mass;
        }
        String toJson() {
            return "{\"x\":"+x+",\"y\":"+y+",\"vx\":"+vx+",\"vy\":"+vy+",\"r\":"+r+",\"mass\":"+mass+"}";
        }
    }

    static class SimpleJson {
        final String s;
        SimpleJson(String s){this.s=s;}
        String getString(String key){return extractString(key);}
        double getDouble(String key){return Double.parseDouble(extractNumber(key));}
        World getWorld(String key){
            String k = "\""+key+"\"";
            int i = s.indexOf(k);
            int start = s.indexOf('{', i);
            int end = findMatchingBrace(start);
            String sub = s.substring(start, end+1);
            double width = extractNumberFrom(sub, "width");
            double height = extractNumberFrom(sub, "height");
            Body[] bodies = parseBodies(sub);
            return new World(width, height, bodies);
        }
        Body[] parseBodies(String sub){
            String k = "\"bodies\"";
            int i = sub.indexOf(k);
            int start = sub.indexOf('[', i);
            int end = sub.indexOf(']', start);
            String arr = sub.substring(start+1, end);
            if (arr.trim().isEmpty()) return new Body[0];
            String[] parts = arr.split("\\},\\{");
            Body[] out = new Body[parts.length];
            for (int j=0;j<parts.length;j++){
                String el = parts[j];
                if (!el.startsWith("{")) el = "{"+el;
                if (!el.endsWith("}")) el = el+"}";
                double x = extractNumberFrom(el,"x");
                double y = extractNumberFrom(el,"y");
                double vx = extractNumberFrom(el,"vx");
                double vy = extractNumberFrom(el,"vy");
                double r = extractNumberFrom(el,"r");
                double mass = extractNumberFrom(el,"mass");
                out[j] = new Body(x,y,vx,vy,r,mass);
            }
            return out;
        }
        int findMatchingBrace(int start){
            int depth=0;
            for (int i=start;i<s.length();i++){
                char c=s.charAt(i);
                if (c=='{') depth++;
                else if (c=='}'){
                    depth--;
                    if (depth==0) return i;
                }
            }
            return s.length()-1;
        }
        String extractString(String key){
            String k="\""+key+"\"";
            int i=s.indexOf(k);
            int start=s.indexOf('"', i+k.length());
            int end=s.indexOf('"', start+1);
            return s.substring(start+1,end);
        }
        String extractNumber(String key){
            String k="\""+key+"\"";
            int i=s.indexOf(k);
            int start=s.indexOf(':', i);
            int end=findDelimiter(start);
            return s.substring(start+1,end).trim();
        }
        double extractNumberFrom(String sub,String key){
            String k="\""+key+"\"";
            int i=sub.indexOf(k);
            int start=sub.indexOf(':', i);
            int end=findDelimiter(sub, start);
            return Double.parseDouble(sub.substring(start+1,end).trim());
        }
        int findDelimiter(int start){
            int end=s.indexOf(',', start+1);
            if (end<0) end=s.indexOf('}', start+1);
            return end;
        }
        int findDelimiter(String ss,int start){
            int end=ss.indexOf(',', start+1);
            if (end<0) end=ss.indexOf('}', start+1);
            return end;
        }
    }
}

