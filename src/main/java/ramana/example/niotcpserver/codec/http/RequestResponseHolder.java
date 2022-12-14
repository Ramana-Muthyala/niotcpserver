package ramana.example.niotcpserver.codec.http;

import ramana.example.niotcpserver.codec.http.request.RequestMessage;
import ramana.example.niotcpserver.codec.http.response.ResponseMessage;

public class RequestResponseHolder {
    public final RequestMessage requestMessage;
    public final ResponseMessage responseMessage;

    public RequestResponseHolder(RequestMessage requestMessage, ResponseMessage responseMessage) {
        this.requestMessage = requestMessage;
        this.responseMessage = responseMessage;
    }
}
