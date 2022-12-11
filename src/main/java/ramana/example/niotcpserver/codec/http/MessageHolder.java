package ramana.example.niotcpserver.codec.http;

import ramana.example.niotcpserver.codec.http.request.RequestMessage;
import ramana.example.niotcpserver.codec.http.response.ResponseMessage;

public class MessageHolder {
    public final RequestMessage requestMessage;
    public final ResponseMessage responseMessage;

    public MessageHolder(RequestMessage requestMessage) {
        this.requestMessage = requestMessage;
        this.responseMessage = new ResponseMessage();
    }
}
