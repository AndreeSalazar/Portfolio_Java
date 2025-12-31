package plugins;

public interface Plugin {
    String id();
    String invoke(String request);
}

