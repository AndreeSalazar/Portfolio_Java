package toolchain;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.security.MessageDigest;

public class NativeTools {
    public enum Mode { JNI, IPC, JAVA }
    private Mode mode = Mode.JAVA;
    private Process ipcProcess;

    public NativeTools() {
        init();
    }

    private native String acExecuteJNI(String requestJson);

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
        String explicit = System.getenv("TOOLCHAIN_NATIVE_LIB");
        try {
            if (explicit != null && !explicit.isBlank()) {
                System.load(explicit);
                return true;
            }
            System.loadLibrary("asset_core");
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private boolean tryStartIpc() {
        try {
            String bin = System.getenv("TOOLCHAIN_IPC_BIN");
            if (bin == null || bin.isBlank()) {
                Path p = Path.of("toolchain-gamedev", "rust-core", "target", "debug", "asset-ipc.exe");
                if (!Files.exists(p)) return false;
                bin = p.toString();
            }
            ProcessBuilder pb = new ProcessBuilder(bin);
            pb.redirectErrorStream(true);
            ipcProcess = pb.start();
            Thread.sleep(300);
            try (Socket s = new Socket()) {
                s.connect(new InetSocketAddress("127.0.0.1", 9092), 300);
                return true;
            } catch (IOException e) {
                return false;
            }
        } catch (Throwable t) {
            return false;
        }
    }

    public Result compress(byte[] data, String algo) {
        String b64 = Base64.getEncoder().encodeToString(data);
        String req = "{\"op\":\"compress\",\"data_b64\":\"" + b64 + "\",\"algo\":\"" + algo + "\"}";
        String out = execute(req);
        int sizeBefore = (int) getLong(out, "size_before");
        int sizeAfter = (int) getLong(out, "size_after");
        String algoOut = getString(out, "algo");
        String ob64 = getString(out, "data_b64");
        byte[] outBytes = Base64.getDecoder().decode(ob64);
        return new Result(sizeBefore, sizeAfter, algoOut, outBytes, out);
    }

    public byte[] decompress(byte[] data, String algo) {
        String b64 = Base64.getEncoder().encodeToString(data);
        String req = "{\"op\":\"decompress\",\"data_b64\":\"" + b64 + "\",\"algo\":\"" + algo + "\"}";
        String out = execute(req);
        String ob64 = getString(out, "data_b64");
        return Base64.getDecoder().decode(ob64);
    }

    public String hash(byte[] data, String algo) {
        String b64 = Base64.getEncoder().encodeToString(data);
        String req = "{\"op\":\"hash\",\"data_b64\":\"" + b64 + "\",\"algo\":\"" + algo + "\"}";
        String out = execute(req);
        return getString(out, "hex");
    }

    private String execute(String requestJson) {
        switch (mode) {
            case JNI:
                return acExecuteJNI(requestJson);
            case IPC:
                return executeIpc(requestJson);
            default:
                return executeJava(requestJson);
        }
    }

    private String executeIpc(String requestJson) {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress("127.0.0.1", 9092), 500);
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
        if (requestJson.contains("\"op\":\"compress\"")) {
            String algo = getString(requestJson, "algo");
            String b64 = getString(requestJson, "data_b64");
            byte[] data = Base64.getDecoder().decode(b64);
            byte[] out = algo.equals("gzip") ? gzip(data) : data;
            String ob64 = Base64.getEncoder().encodeToString(out);
            return "{\"ok\":true,\"result\":{\"size_before\":" + data.length + ",\"size_after\":" + out.length + ",\"algo\":\"" + algo + "\",\"data_b64\":\"" + ob64 + "\"},\"mode\":\"JAVA\"}";
        }
        if (requestJson.contains("\"op\":\"decompress\"")) {
            String algo = getString(requestJson, "algo");
            String b64 = getString(requestJson, "data_b64");
            byte[] data = Base64.getDecoder().decode(b64);
            byte[] out = algo.equals("gzip") ? gunzip(data) : data;
            String ob64 = Base64.getEncoder().encodeToString(out);
            return "{\"ok\":true,\"result\":{\"size_after\":" + out.length + ",\"algo\":\"" + algo + "\",\"data_b64\":\"" + ob64 + "\"},\"mode\":\"JAVA\"}";
        }
        if (requestJson.contains("\"op\":\"hash\"")) {
            String algo = getString(requestJson, "algo");
            String b64 = getString(requestJson, "data_b64");
            byte[] data = Base64.getDecoder().decode(b64);
            String hex = algo.equals("sha256") ? sha256(data) : "";
            return "{\"ok\":true,\"result\":{\"algo\":\"" + algo + "\",\"hex\":\"" + hex + "\"},\"mode\":\"JAVA\"}";
        }
        return "{\"ok\":false,\"result\":{\"error\":\"invalid_request\"},\"mode\":\"JAVA\"}";
    }

    private static byte[] gzip(byte[] data) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            GZIPOutputStream gos = new GZIPOutputStream(bos);
            gos.write(data);
            gos.close();
            return bos.toByteArray();
        } catch (IOException e) {
            return data;
        }
    }

    private static byte[] gunzip(byte[] data) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            GZIPInputStream gis = new GZIPInputStream(bis);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = gis.read(buf)) > 0) bos.write(buf, 0, n);
            return bos.toByteArray();
        } catch (IOException e) {
            return data;
        }
    }

    private static String sha256(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private static String getString(String s, String key) {
        String k = "\"" + key + "\"";
        int i = s.indexOf(k);
        int start = s.indexOf('"', i + k.length());
        int end = s.indexOf('"', start + 1);
        return s.substring(start + 1, end);
    }

    private static long getLong(String s, String key) {
        String k = "\"" + key + "\"";
        int i = s.indexOf(k);
        int start = s.indexOf(':', i);
        int end = s.indexOf(',', start + 1);
        if (end < 0) end = s.indexOf('}', start + 1);
        return Long.parseLong(s.substring(start + 1, end).trim());
    }

    public static class Result {
        public final int sizeBefore;
        public final int sizeAfter;
        public final String algo;
        public final byte[] data;
        public final String raw;
        public Result(int sizeBefore, int sizeAfter, String algo, byte[] data, String raw) {
            this.sizeBefore = sizeBefore;
            this.sizeAfter = sizeAfter;
            this.algo = algo;
            this.data = data;
            this.raw = raw;
        }
    }
}

