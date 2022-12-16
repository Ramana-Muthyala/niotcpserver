package ramana.example.niotcpserver.codec.http.response;

import ramana.example.niotcpserver.codec.http.Message;
import ramana.example.niotcpserver.codec.http.request.Field;

import java.util.ArrayList;
import java.util.List;

public class ResponseMessage extends Message {
    public int statusCode;
    public List<Field> headers = new ArrayList<>();
}
