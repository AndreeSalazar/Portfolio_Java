package os;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class OSRustAdapter {
    public enum Mode { JNI, IPC, JAVA }
    private Mode mode = Mode.JAVA;
    private Process ipcProcess;

    private final Map<Integer, byte[]> mem = new HashMap<>();
    private int nextId = 1;

    public OSRustAdapter() {
        init();
    }

    private native String osExecuteJNI(String requestJson);

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
        String explicit = System.getenv("OS_NATIVE_LIB");
        try {
            if (explicit != null && !explicit.isBlank()) {
                System.load(explicit);
                return true;
            }
            System.loadLibrary("os_core");
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private boolean tryStartIpc() {
        try {
            String bin = System.getenv("OS_IPC_BIN");
            if (bin == null || bin.isBlank()) {
                Path p = Path.of("Sistema Operativo de Aplicación (Java)", "rust-core", "target", "debug", "os-ipc.exe");
                if (!Files.exists(p)) return false;
                bin = p.toString();
            }
            ProcessBuilder pb = new ProcessBuilder(bin);
            pb.redirectErrorStream(true);
            ipcProcess = pb.start();
            Thread.sleep(300);
            try (Socket s = new Socket()) {
                s.connect(new InetSocketAddress("127.0.0.1", 9096), 300);
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
                return osExecuteJNI(requestJson);
            case IPC:
                return executeIpc(requestJson);
            default:
                return executeJava(requestJson);
        }
    }

    private String executeIpc(String requestJson) {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress("127.0.0.1", 9096), 500);
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
        if (requestJson.contains("\"op\":\"alloc\"")) {
            int size = (int) extractNumber(requestJson, "size");
            int id = nextId++;
            mem.put(id, new byte[Math.max(0, size)]);
            return "{\"ok\":true,\"result\":{\"id\":" + id + "},\"mode\":\"JAVA\"}";
        }
        if (requestJson.contains("\"op\":\"write\"")) {
            int id = (int) extractNumber(requestJson, "id");
            String dataB64 = extractString(requestJson, "data");
            byte[] bytes = Base64.getDecoder().decode(dataB64);
            mem.put(id, bytes);
            return "{\"ok\":true,\"result\":{},\"mode\":\"JAVA\"}";
        }
        if (requestJson.contains("\"op\":\"read\"")) {
            int id = (int) extractNumber(requestJson, "id");
            byte[] bytes = mem.getOrDefault(id, new byte[0]);
            String out = Base64.getEncoder().encodeToString(bytes);
            return "{\"ok\":true,\"result\":{\"data\":\"" + out + "\"},\"mode\":\"JAVA\"}";
        }
        if (requestJson.contains("\"op\":\"free\"")) {
            int id = (int) extractNumber(requestJson, "id");
            mem.remove(id);
            return "{\"ok\":true,\"result\":{},\"mode\":\"JAVA\"}";
        }
        if (requestJson.contains("\"op\":\"io\"")) {
            String kind = extractString(requestJson, "kind");
            String data = extractString(requestJson, "data");
            int len = data != null ? data.length() : 0;
            return "{\"ok\":true,\"result\":{\"kind\":\"" + kind + "\",\"len\":" + len + "},\"mode\":\"JAVA\"}";
        }
        return "{\"ok\":false,\"result\":{\"error\":\"invalid_request\"},\"mode\":\"JAVA\"}";
    }

    private static double extractNumber(String s, String key) {
        String k = "\"" + key + "\"";
        int i = s.indexOf(k);
        int start = s.indexOf(':', i);
        int j = start + 1;
        while (j < s.length()) {
            char c = s.charAt(j);
            if ((c >= '0' && c <= '9') || c == '-' || c == '+' || c == '.' || c == 'e' || c == 'E') j++;
            else break;
        }
        try { return Double.parseDouble(s.substring(start + 1, j).trim()); } catch (Exception e) { return 0.0; }
    }

    private static String extractString(String s, String key) {
        String k = "\"" + key + "\"";
        int i = s.indexOf(k);
        int start = s.indexOf(':', i);
        int q1 = s.indexOf('"', start + 1);
        int q2 = s.indexOf('"', q1 + 1);
        if (q1 < 0 || q2 < 0) return null;
        return s.substring(q1 + 1, q2);
    }

    public void shutdown() {
        if (ipcProcess != null) {
            ipcProcess.destroy();
        }
    }
}
