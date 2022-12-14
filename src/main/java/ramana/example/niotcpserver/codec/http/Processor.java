package ramana.example.niotcpserver.codec.http;

public interface Processor {
    void process(RequestResponseHolder requestResponseHolder);
}
