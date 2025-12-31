package os;

public class Main {
    public static void main(String[] args) {
        Kernel k = new Kernel();
        k.register(new Kernel.ComputeProc("P1"));
        k.register(new Kernel.IOProc("P2"));
        k.start();
        for (int i=0;i<5;i++){
            k.step();
            String s = "{\"tick\":" + k.tick() + ",\"status\":\"ok\"}";
            System.out.println(s);
        }
        k.shutdown();
    }
}
