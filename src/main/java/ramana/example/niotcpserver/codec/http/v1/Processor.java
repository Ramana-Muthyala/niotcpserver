package ramana.example.niotcpserver.codec.http.v1;

import ramana.example.niotcpserver.codec.http.request.v1.RequestMessage;
import ramana.example.niotcpserver.codec.http.response.ResponseMessage;

public interface Processor {
    void process(RequestMessage requestMessage, ResponseMessage responseMessage);
}
