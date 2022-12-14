package ramana.example.niotcpserver.codec.http;

public class Message {
    public byte[] body;

    public Message() {}
    public Message(byte[] body) {
        this.body = body;
    }
}
