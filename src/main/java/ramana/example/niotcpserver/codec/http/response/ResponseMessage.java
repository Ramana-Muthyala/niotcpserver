package ramana.example.niotcpserver.codec.http.response;

import ramana.example.niotcpserver.codec.http.Message;

public class ResponseMessage extends Message {
    public ResponseMessage(byte[] body) {
        super(body);
    }
}
