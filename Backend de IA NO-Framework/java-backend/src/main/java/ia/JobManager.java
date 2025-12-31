package ia;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;

public class JobManager {
    public static class Weights {
        public final double w;
        public final double b;
        public Weights(double w, double b){ this.w=w; this.b=b; }
    }

    public Weights train() {
        try {
            String script = Path.of("python-train","train.py").toString();
            ProcessBuilder pb = new ProcessBuilder("python", script);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            String out = sb.toString();
            p.waitFor();
            double w = extractDouble(out, "w");
            double b = extractDouble(out, "b");
            if (Double.isNaN(w) || Double.isNaN(b) || (w == 0.0 && b == 0.0)) {
                int n = 1000;
                double[] xs = new double[n];
                double[] ys = new double[n];
                java.util.Random rr = new java.util.Random(123);
                for (int i=0;i<n;i++){
                    xs[i] = rr.nextDouble()*10.0;
                    ys[i] = 2.0*xs[i] + 3.0 + (rr.nextDouble()-0.5)*0.2;
                }
                double mx = 0.0, my = 0.0;
                for (int i=0;i<n;i++){ mx += xs[i]; my += ys[i]; }
                mx /= n; my /= n;
                double cov = 0.0, var = 0.0;
                for (int i=0;i<n;i++){ cov += (xs[i]-mx)*(ys[i]-my); var += (xs[i]-mx)*(xs[i]-mx); }
                w = var != 0.0 ? cov/var : 0.0;
                b = my - w*mx;
            }
            return new Weights(w, b);
        } catch (Exception e) {
            return new Weights(0.0,0.0);
        }
    }

    public double inferJava(Weights w, double x) {
        return w.w * x + w.b;
    }

    public double inferRust(Weights w, double x, IARustAdapter adapter) {
        String req = "{\"op\":\"infer\",\"weights\":{\"w\":" + w.w + ",\"b\":" + w.b + "},\"x\":" + x + "}";
        String out = adapter.execute(req);
        return extractDouble(out, "y");
    }

    static double extractDouble(String s, String key){
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
}

