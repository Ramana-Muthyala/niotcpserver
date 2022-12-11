package ramana.example.niotcpserver.codec.http;

import ramana.example.niotcpserver.codec.http.request.HttpRequestMessage;
import ramana.example.niotcpserver.codec.http.response.HttpResponseMessage;

public class HttpMessageHolder {
    public final HttpRequestMessage httpRequestMessage;
    public final HttpResponseMessage httpResponseMessage;

    public HttpMessageHolder(HttpRequestMessage httpRequestMessage) {
        this.httpRequestMessage = httpRequestMessage;
        this.httpResponseMessage = new HttpResponseMessage();
    }
}
