package plugins;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.*;

public class PluginManager {
    private final Map<String, Plugin> plugins = new HashMap<>();

    public void register(Plugin p) {
        plugins.put(p.id(), p);
    }

    public Plugin loadJavaPlugin(String className, Path dir) {
        try {
            URL u = dir.toUri().toURL();
            URLClassLoader cl = new URLClassLoader(new URL[]{u}, Plugin.class.getClassLoader());
            Class<?> c = Class.forName(className, true, cl);
            Object o = c.getDeclaredConstructor().newInstance();
            if (!(o instanceof Plugin)) throw new IllegalArgumentException();
            Plugin p = (Plugin) o;
            register(p);
            return p;
        } catch (Throwable t) {
            return null;
        }
    }

    public String invoke(String id, String request) {
        Plugin p = plugins.get(id);
        if (p == null) return "{\"ok\":false,\"result\":{\"error\":\"plugin_not_found\"}}";
        return p.invoke(request);
    }

    public Set<String> list() {
        return Collections.unmodifiableSet(plugins.keySet());
    }
}

