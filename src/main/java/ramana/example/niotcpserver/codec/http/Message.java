package ramana.example.niotcpserver.codec.http;

public class Message {
    public final byte[] body;

    public Message(byte[] body) {
        this.body = body;
    }
}
