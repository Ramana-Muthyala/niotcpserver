package ramana.example.niotcpserver.codec.http.request;

public class Header {
    public final String name;
    public final String value;

    public Header(String name, String value) {
        this.name = name;
        this.value = value;
    }
}
