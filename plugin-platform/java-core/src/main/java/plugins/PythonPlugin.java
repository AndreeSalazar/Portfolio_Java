package plugins;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;

public class PythonPlugin implements Plugin {
    private final String id;
    private final Path script;

    public PythonPlugin(String id, Path script) {
        this.id = id;
        this.script = script;
    }

    public String id() { return id; }

    public String invoke(String request) {
        try {
            String b64 = Base64.getEncoder().encodeToString(request.getBytes(StandardCharsets.UTF_8));
            ProcessBuilder pb = new ProcessBuilder("python", script.toString(), b64);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
            String out = br.readLine();
            p.waitFor();
            if (out == null) return "{\"ok\":false,\"result\":{\"error\":\"python_no_output\"},\"mode\":\"PYTHON\"}";
            return out;
        } catch (Throwable t) {
            return "{\"ok\":false,\"result\":{\"error\":\"python_error\"},\"mode\":\"PYTHON\"}";
        }
    }
}

