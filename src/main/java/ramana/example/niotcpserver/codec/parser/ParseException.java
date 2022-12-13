package ramana.example.niotcpserver.codec.parser;

public class ParseException extends Exception {
    public ParseException() {
    }

    public ParseException(Throwable cause) {
        super(cause);
    }

    public ParseException(String message) {
        super(message);
    }
}
