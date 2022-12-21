package ramana.example.niotcpserver.codec.http.request.v1;

import ramana.example.niotcpserver.codec.http.Message;
import ramana.example.niotcpserver.codec.http.request.Field;

import java.util.List;
import java.util.Map;

public class RequestMessage extends Message {
    public String method;
    public String path;
    public Map<String, String> queryParameters;
    public List<Field> headers;
}
