package ramana.example.niotcpserver.codec.http.request;

import ramana.example.niotcpserver.codec.http.Message;

import java.util.List;

public class RequestMessage extends Message {
    public final String method;
    public final String path;
    public final String queryString;
    public final List<Header> headers;

    public RequestMessage(RequestLine requestLine, List<Header> headers) {
        this.method = requestLine.method;
        this.path = requestLine.path;
        this.queryString = requestLine.queryString;
        this.headers = headers;
    }
}
