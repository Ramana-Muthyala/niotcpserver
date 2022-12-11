package ramana.example.niotcpserver.codec.http.request;

public class RequestLine {
    public final String method;
    public final String path;
    public final String queryString;

    public RequestLine(String method, String path, String queryString) {
        this.method = method;
        this.path = path;
        this.queryString = queryString;
    }
}
