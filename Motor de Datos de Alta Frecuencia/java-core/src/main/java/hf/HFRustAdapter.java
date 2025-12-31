package hf;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class HFRustAdapter {
    public enum Mode { JNI, IPC, JAVA }
    private Mode mode = Mode.JAVA;
    private Process ipcProcess;

    public HFRustAdapter() {
        init();
    }

    private native String hfExecuteJNI(String requestJson);

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
        String explicit = System.getenv("HF_NATIVE_LIB");
        try {
            if (explicit != null && !explicit.isBlank()) {
                System.load(explicit);
                return true;
            }
            System.loadLibrary("hf_core");
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private boolean tryStartIpc() {
        try {
            String bin = System.getenv("HF_IPC_BIN");
            if (bin == null || bin.isBlank()) {
                Path p = Path.of("Motor de Datos de Alta Frecuencia", "rust-core", "target", "debug", "hf-ipc.exe");
                if (!Files.exists(p)) return false;
                bin = p.toString();
            }
            ProcessBuilder pb = new ProcessBuilder(bin);
            pb.redirectErrorStream(true);
            ipcProcess = pb.start();
            Thread.sleep(300);
            try (Socket s = new Socket()) {
                s.connect(new InetSocketAddress("127.0.0.1", 9094), 300);
                return true;
            } catch (IOException e) {
                return false;
            }
        } catch (Throwable t) {
            return false;
        }
    }

    public String parseCsv(String csv) {
        String req = "{\"op\":\"parse\",\"csv\":\"" + csv + "\"}";
        switch (mode) {
            case JNI:
                return hfExecuteJNI(req);
            case IPC:
                return executeIpc(req);
            default:
                return parseJava(csv);
        }
    }

    private String executeIpc(String requestJson) {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress("127.0.0.1", 9094), 500);
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

    private String parseJava(String csv) {
        String[] f = csv.split(",", -1);
        long ts = f.length>0 ? parseLong(f[0]) : 0;
        String symbol = f.length>1 ? f[1] : "";
        double price = f.length>2 ? parseDouble(f[2]) : 0.0;
        long qty = f.length>3 ? parseLong(f[3]) : 0;
        double notional = price * qty;
        return "{\"ok\":true,\"result\":{\"ts\":"+ts+",\"symbol\":\""+symbol+"\",\"price\":"+price+",\"qty\":"+qty+",\"notional\":"+notional+"},\"mode\":\"JAVA\"}";
    }

    private long parseLong(String s){ try { return Long.parseLong(s.trim()); } catch(Exception e){ return 0; } }
    private double parseDouble(String s){ try { return Double.parseDouble(s.trim()); } catch(Exception e){ return 0.0; } }

    public void shutdown() {
        if (ipcProcess != null) {
            ipcProcess.destroy();
        }
    }
}

