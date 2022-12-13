package ramana.example.niotcpserver.codec.parser;

public class Util {
    public static final byte[] GET = "GET".getBytes();
    public static final byte[] HEAD = "HEAD".getBytes();
    public static final byte[] POST = "POST".getBytes();
    public static final byte[] PUT = "PUT".getBytes();
    public static final byte[] DELETE = "DELETE".getBytes();
    public static final byte[] CONNECT = "CONNECT".getBytes();
    public static final byte[] OPTIONS = "OPTIONS".getBytes();
    public static final byte[] TRACE = "TRACE".getBytes();
    public static final byte SP = ' ';
    public static final byte DOT = '.';
    public static final byte COMMA = ',';
    public static final byte COLON = ':';
    public static final byte CR = '\r';
    public static final byte LF = '\n';
    public static final byte[] CRLF = new byte[] {'\r', '\n'};
    public static final byte[] HTTP_SLASH = "HTTP/".getBytes();
    public static final int REQ_TARGET_MAX_LEN = 512;
    public static final int REQ_HEADER_MAX_LEN = 128;
    public static final int REQ_HEADER_VAL_MAX_LEN = 512;

    public static OneByte createSpaceParser() {
        return new OneByte(SP);
    }

    public static EitherOfBytes createDigitParser() {
        return new EitherOfBytes(new byte[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'});
    }

    public static OneByte createDotParser() {
        return new OneByte(DOT);
    }

    public static ByteSequence createCRLFParser() {
        return new ByteSequence(CRLF);
    }
}
