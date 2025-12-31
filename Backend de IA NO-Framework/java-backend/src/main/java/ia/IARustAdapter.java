package ia;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class IARustAdapter {
    public enum Mode { JNI, IPC, JAVA }
    private Mode mode = Mode.JAVA;
    private Process ipcProcess;

    public IARustAdapter() {
        init();
    }

    private native String iaExecuteJNI(String requestJson);

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
        String explicit = System.getenv("IA_NATIVE_LIB");
        try {
            if (explicit != null && !explicit.isBlank()) {
                System.load(explicit);
                return true;
            }
            System.loadLibrary("ia_infer");
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private boolean tryStartIpc() {
        try {
            String bin = System.getenv("IA_IPC_BIN");
            if (bin == null || bin.isBlank()) {
                Path p = Path.of("Backend de IA NO-Framework", "rust-infer", "target", "debug", "infer-ipc.exe");
                if (!Files.exists(p)) return false;
                bin = p.toString();
            }
            ProcessBuilder pb = new ProcessBuilder(bin);
            pb.redirectErrorStream(true);
            ipcProcess = pb.start();
            Thread.sleep(300);
            try (Socket s = new Socket()) {
                s.connect(new InetSocketAddress("127.0.0.1", 9095), 300);
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
                return iaExecuteJNI(requestJson);
            case IPC:
                return executeIpc(requestJson);
            default:
                return executeJava(requestJson);
        }
    }

    private String executeIpc(String requestJson) {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress("127.0.0.1", 9095), 500);
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
        if (requestJson.contains("\"op\":\"infer\"")) {
            double w = extractDouble(requestJson, "w");
            double b = extractDouble(requestJson, "b");
            double x = extractDouble(requestJson, "x");
            double y = w * x + b;
            return "{\"ok\":true,\"result\":{\"y\":" + y + "},\"mode\":\"JAVA\"}";
        }
        if (requestJson.contains("\"op\":\"infer_batch\"")) {
            double w = extractDouble(requestJson, "w");
            double b = extractDouble(requestJson, "b");
            int arrStart = requestJson.indexOf("\"xs\"");
            int s = requestJson.indexOf('[', arrStart);
            int e = requestJson.indexOf(']', s);
            String arr = requestJson.substring(s+1, e);
            String[] parts = arr.split(",");
            StringBuilder out = new StringBuilder();
            out.append("[");
            for (int i=0;i<parts.length;i++){
                double x = Double.parseDouble(parts[i].trim());
                double y = w * x + b;
                if (i>0) out.append(",");
                out.append(y);
            }
            out.append("]");
            return "{\"ok\":true,\"result\":{\"ys\":" + out + "},\"mode\":\"JAVA\"}";
        }
        return "{\"ok\":false,\"result\":{\"error\":\"invalid_request\"},\"mode\":\"JAVA\"}";
    }

    private static double extractDouble(String s, String key) {
        String k = "\"" + key + "\"";
        int i = s.indexOf(k);
        int start = s.indexOf(':', i);
        int j = start + 1;
        while (j < s.length()) {
            char c = s.charAt(j);
            if ((c >= '0' && c <= '9') || c == '-' || c == '+' || c == '.' || c == 'e' || c == 'E') j++;
            else break;
        }
        return Double.parseDouble(s.substring(start + 1, j).trim());
    }

    public void shutdown() {
        if (ipcProcess != null) {
            ipcProcess.destroy();
        }
    }
}

