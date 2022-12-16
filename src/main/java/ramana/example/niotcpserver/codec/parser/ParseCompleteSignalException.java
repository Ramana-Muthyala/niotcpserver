package ramana.example.niotcpserver.codec.parser;

public class ParseCompleteSignalException extends ParseException {
    public ParseCompleteSignalException(Throwable throwable) {
        super(throwable);
    }

    public ParseCompleteSignalException() {
    }
}
