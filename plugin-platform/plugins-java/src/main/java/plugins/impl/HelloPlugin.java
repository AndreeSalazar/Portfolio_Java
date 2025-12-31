package plugins.impl;

import plugins.Plugin;

public class HelloPlugin implements Plugin {
    public String id() { return "plugins.impl.HelloPlugin"; }
    public String invoke(String request) {
        if (request.contains("\"op\":\"echo\"")) {
            String msg = extractString(request, "msg");
            String up = msg.toUpperCase();
            return "{\"ok\":true,\"result\":{\"msg\":\"" + up + "\"},\"mode\":\"JAVA_PLUGIN\"}";
        }
        return "{\"ok\":false,\"result\":{\"error\":\"invalid_request\"},\"mode\":\"JAVA_PLUGIN\"}";
    }
    static String extractString(String s, String key){
        String k="\""+key+"\"";
        int i=s.indexOf(k);
        int start=s.indexOf('"', i+k.length());
        int end=s.indexOf('"', start+1);
        return s.substring(start+1,end);
    }
}

