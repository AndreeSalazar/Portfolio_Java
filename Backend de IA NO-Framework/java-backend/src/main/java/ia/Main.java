package ia;

public class Main {
    public static void main(String[] args) throws Exception {
        JobManager jm = new JobManager();
        JobManager.Weights w = jm.train();
        IARustAdapter adapter = new IARustAdapter();
        double x = 7.5;
        double yJava = jm.inferJava(w, x);
        double yRust = jm.inferRust(w, x, adapter);
        System.out.println("{\"weights\":{\"w\":" + w.w + ",\"b\":" + w.b + "},\"x\":" + x + ",\"y_java\":" + yJava + ",\"y_rust\":" + yRust + "}");
        adapter.shutdown();
    }
}

