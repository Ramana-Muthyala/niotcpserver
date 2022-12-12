package ramana.example.niotcpserver.codec.http.request;

import java.util.HashMap;
import java.util.Map;

public class RequestLine {
    public final String method;
    public final String path;
    public final Map<String, String> queryParameters = new HashMap<>();

    public RequestLine(String method, String path, String queryString) {
        this.method = method;
        this.path = path;
        if(queryString == null  ||  queryString.equals("")) return;
        parseQueryString(queryString);
    }

    private void parseQueryString(String queryString) {
        int begin = 0;
        int end;
        while((end = queryString.indexOf('&', begin)) != -1) {
            if(begin != end) {
                parseQueryParam(queryString.substring(begin, end));
            }
            begin = end + 1;
            if(begin == queryString.length()) break;
        }
        if(begin == 0) {
            parseQueryParam(queryString);
            return;
        }
        if(begin < queryString.length()) {
            parseQueryParam(queryString.substring(begin));
        }
    }

    private void parseQueryParam(String input) {
        int index = input.indexOf('=');
        if(index == -1) {
            queryParameters.put(input, null);
        } else {
            if(index == 0) return;
            String value = (index + 1) < input.length() ? input.substring(index + 1) : null;
            queryParameters.put(input.substring(0, index), value);
        }
    }
}
