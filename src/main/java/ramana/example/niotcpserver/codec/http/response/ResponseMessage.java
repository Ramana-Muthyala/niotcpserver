package ramana.example.niotcpserver.codec.http.response;

import ramana.example.niotcpserver.codec.http.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ResponseMessage extends Message {
    public int statusCode;
    public Map<String, ArrayList<String>> headers = new HashMap<>();
}
