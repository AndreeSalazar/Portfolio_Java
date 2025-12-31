package sim;

public class Main {
    public static void main(String[] args) throws Exception {
        NativeSim sim = new NativeSim();
        double width = 800, height = 600;
        NativeSim.Body b1 = new NativeSim.Body(100, 300, 120, 0, 20, 2);
        NativeSim.Body b2 = new NativeSim.Body(700, 300, -120, 0, 20, 2);
        NativeSim.Body b3 = new NativeSim.Body(400, 100, 0, 180, 15, 1.5);
        NativeSim.World world = new NativeSim.World(width, height, new NativeSim.Body[]{b1, b2, b3});
        double dt = 0.016;
        for (int frame = 0; frame < 240; frame++) {
            String req = "{\"op\":\"step\",\"world\":" + world.toJson() + ",\"dt\":" + dt + "}";
            String out = sim.execute(req);
            world = parseWorld(out);
            String mreq = "{\"op\":\"metrics\",\"world\":" + world.toJson() + "}";
            String metrics = sim.execute(mreq);
            double k = getDoubleValue(metrics, "kinetic_energy");
            Thread.sleep(16);
            System.out.println(metrics);
        }
        sim.shutdown();
    }

    static NativeSim.World parseWorld(String out) {
        String k = "\"world\"";
        int i = out.indexOf(k);
        int start = out.indexOf('{', i);
        int end = findMatchingBrace(out, start);
        String sub = out.substring(start, end + 1);
        double width = getDoubleValue(sub, "width");
        double height = getDoubleValue(sub, "height");
        NativeSim.Body[] bodies = parseBodies(sub);
        return new NativeSim.World(width, height, bodies);
    }

    static NativeSim.Body[] parseBodies(String sub) {
        String k = "\"bodies\"";
        int i = sub.indexOf(k);
        int start = sub.indexOf('[', i);
        int end = sub.indexOf(']', start);
        String arr = sub.substring(start + 1, end);
        if (arr.trim().isEmpty()) return new NativeSim.Body[0];
        String[] parts = arr.split("\\},\\{");
        NativeSim.Body[] out = new NativeSim.Body[parts.length];
        for (int j = 0; j < parts.length; j++) {
            String el = parts[j];
            if (!el.startsWith("{")) el = "{" + el;
            if (!el.endsWith("}")) el = el + "}";
            double x = getDoubleValue(el, "x");
            double y = getDoubleValue(el, "y");
            double vx = getDoubleValue(el, "vx");
            double vy = getDoubleValue(el, "vy");
            double r = getDoubleValue(el, "r");
            double mass = getDoubleValue(el, "mass");
            out[j] = new NativeSim.Body(x, y, vx, vy, r, mass);
        }
        return out;
    }

    static int findMatchingBrace(String s, int start) {
        int depth = 0;
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return s.length() - 1;
    }

    static String extractNumber(String s, String key, boolean inSub) {
        String k = "\"" + key + "\"";
        int i = s.indexOf(k);
        int start = s.indexOf(':', i);
        int end = s.indexOf(',', start + 1);
        if (end < 0) end = s.indexOf('}', start + 1);
        return s.substring(start + 1, end).trim();
    }

    static double getDoubleValue(String s, String key) {
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
}
