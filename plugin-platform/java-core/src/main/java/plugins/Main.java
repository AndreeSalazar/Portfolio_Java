package plugins;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        PluginManager pm = new PluginManager();
        pm.loadJavaPlugin("plugins.impl.HelloPlugin", Path.of("plugins-java", "out"));
        pm.register(new PythonPlugin("python.hello", Path.of("plugins-python", "hello.py")));
        pm.register(new NativeRustPlugin("rust.math"));

        String echoReq = "{\"op\":\"echo\",\"msg\":\"hola\"}";
        String sumReq = "{\"op\":\"sum\",\"values\":[1,2,3,4]}";

        String j = pm.invoke("plugins.impl.HelloPlugin", echoReq);
        System.out.println(j);
        String p = pm.invoke("python.hello", echoReq);
        System.out.println(p);
        String r = pm.invoke("rust.math", sumReq);
        System.out.println(r);
    }
}

