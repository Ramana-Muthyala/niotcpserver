package ramana.example.niotcpserver.codec.http;

import ramana.example.niotcpserver.codec.http.request.RequestMessage;
import ramana.example.niotcpserver.codec.http.response.ResponseMessage;

public interface Processor {
    void process(RequestMessage requestMessage, ResponseMessage responseMessage);
}
