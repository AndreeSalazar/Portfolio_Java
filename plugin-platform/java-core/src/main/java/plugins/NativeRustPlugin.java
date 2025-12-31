package plugins;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class NativeRustPlugin implements Plugin {
    public enum Mode { JNI, IPC }
    private final String id;
    private Mode mode = Mode.IPC;
    private Process ipcProcess;

    public NativeRustPlugin(String id) {
        this.id = id;
        init();
    }

    private native String ppExecuteJNI(String requestJson);

    private void init() {
        if (tryLoadNative()) {
            mode = Mode.JNI;
            return;
        }
        if (tryStartIpc()) {
            mode = Mode.IPC;
            return;
        }
    }

    private boolean tryLoadNative() {
        String explicit = System.getenv("PLUGIN_NATIVE_LIB");
        try {
            if (explicit != null && !explicit.isBlank()) {
                System.load(explicit);
                return true;
            }
            System.loadLibrary("plugin_core");
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private boolean tryStartIpc() {
        try {
            String bin = System.getenv("PLUGIN_RUST_IPC_BIN");
            if (bin == null || bin.isBlank()) {
                Path p = Path.of("plugin-platform", "plugins-rust", "target", "debug", "rust-plugin-ipc.exe");
                if (!Files.exists(p)) return false;
                bin = p.toString();
            }
            ProcessBuilder pb = new ProcessBuilder(bin);
            pb.redirectErrorStream(true);
            ipcProcess = pb.start();
            Thread.sleep(300);
            try (Socket s = new Socket()) {
                s.connect(new InetSocketAddress("127.0.0.1", 9093), 300);
                return true;
            } catch (IOException e) {
                return false;
            }
        } catch (Throwable t) {
            return false;
        }
    }

    public String id() { return id; }

    public String invoke(String request) {
        if (mode == Mode.JNI) return ppExecuteJNI(request);
        return invokeIpc(request);
    }

    private String invokeIpc(String request) {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress("127.0.0.1", 9093), 500);
            OutputStream os = s.getOutputStream();
            InputStream is = s.getInputStream();
            os.write((request + "\n").getBytes(StandardCharsets.UTF_8));
            os.flush();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String out = br.readLine();
            if (out == null) return "{\"ok\":false,\"result\":{\"error\":\"ipc_no_response\"},\"mode\":\"IPC\"}";
            return out;
        } catch (IOException e) {
            return "{\"ok\":false,\"result\":{\"error\":\"ipc_error\"},\"mode\":\"IPC\"}";
        }
    }
}

