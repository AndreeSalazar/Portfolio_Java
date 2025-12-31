package runtime;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class NativeRuntime {
    public enum Mode { JNI, IPC, JAVA }

    private Mode mode = Mode.JAVA;
    private Process ipcProcess;

    public NativeRuntime() {
        init();
    }

    private native String rcExecuteJNI(String requestJson);

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
        String explicit = System.getenv("RUNTIME_NATIVE_LIB");
        try {
            if (explicit != null && !explicit.isBlank()) {
                System.load(explicit);
                return true;
            }
            System.loadLibrary("rust_core");
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private boolean tryStartIpc() {
        try {
            String bin = System.getenv("RUST_CORE_IPC_BIN");
            if (bin == null || bin.isBlank()) {
                Path p = Path.of("runtime", "rust-core", "target", "debug", "core-ipc.exe");
                if (!Files.exists(p)) return false;
                bin = p.toString();
            }
            ProcessBuilder pb = new ProcessBuilder(bin);
            pb.redirectErrorStream(true);
            ipcProcess = pb.start();
            Thread.sleep(300);
            try (Socket s = new Socket()) {
                s.connect(new InetSocketAddress("127.0.0.1", 9090), 300);
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
                return rcExecuteJNI(requestJson);
            case IPC:
                return executeIpc(requestJson);
            default:
                return executeJava(requestJson);
        }
    }

    private String executeIpc(String requestJson) {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress("127.0.0.1", 9090), 500);
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
        return JavaCore.process(requestJson);
    }

    public void shutdown() {
        if (ipcProcess != null) {
            ipcProcess.destroy();
        }
    }

    static class JavaCore {
        static String process(String input) {
            try {
                var parser = new SimpleJson(input);
                String op = parser.getString("op");
                if ("sum".equals(op)) {
                    double[] vals = parser.getDoubleArray("values");
                    double sum = 0.0;
                    for (double v : vals) sum += v;
                    return "{\"ok\":true,\"result\":{\"sum\":" + sum + "},\"mode\":\"JAVA\"}";
                }
                if ("simulate".equals(op)) {
                    long steps = parser.getLong("steps");
                    long seed = parser.getLong("seed");
                    java.util.Random r = new java.util.Random(seed);
                    double acc = 0.0;
                    for (long i = 0; i < steps; i++) {
                        double x = r.nextDouble() - 0.5;
                        acc += x;
                    }
                    return "{\"ok\":true,\"result\":{\"acc\":" + acc + ",\"steps\":" + steps + ",\"seed\":" + seed + "},\"mode\":\"JAVA\"}";
                }
                return "{\"ok\":false,\"result\":{\"error\":\"invalid_request\"},\"mode\":\"JAVA\"}";
            } catch (Exception e) {
                return "{\"ok\":false,\"result\":{\"error\":\"invalid_json\"},\"mode\":\"JAVA\"}";
            }
        }
    }

    static class SimpleJson {
        final String s;
        SimpleJson(String s) { this.s = s; }
        String getString(String key) { return extractString(key); }
        long getLong(String key) { return Long.parseLong(extractNumber(key)); }
        double[] getDoubleArray(String key) {
            String k = "\"" + key + "\"";
            int i = s.indexOf(k);
            if (i < 0) throw new IllegalArgumentException();
            int start = s.indexOf('[', i);
            int end = s.indexOf(']', start);
            if (start < 0 || end < 0) throw new IllegalArgumentException();
            String content = s.substring(start + 1, end).trim();
            if (content.isEmpty()) return new double[0];
            String[] parts = content.split(",");
            double[] out = new double[parts.length];
            for (int j = 0; j < parts.length; j++) out[j] = Double.parseDouble(parts[j].trim());
            return out;
        }
        String extractString(String key) {
            String k = "\"" + key + "\"";
            int i = s.indexOf(k);
            if (i < 0) throw new IllegalArgumentException();
            int start = s.indexOf('"', i + k.length());
            int end = s.indexOf('"', start + 1);
            if (start < 0 || end < 0) throw new IllegalArgumentException();
            return s.substring(start + 1, end);
        }
        String extractNumber(String key) {
            String k = "\"" + key + "\"";
            int i = s.indexOf(k);
            if (i < 0) throw new IllegalArgumentException();
            int start = s.indexOf(':', i);
            int end = s.indexOf(',', start + 1);
            if (end < 0) end = s.indexOf('}', start + 1);
            if (start < 0 || end < 0) throw new IllegalArgumentException();
            return s.substring(start + 1, end).trim();
        }
    }
}

