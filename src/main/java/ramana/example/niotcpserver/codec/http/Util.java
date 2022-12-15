package ramana.example.niotcpserver.codec.http;

import ramana.example.niotcpserver.codec.parser.ByteSequence;
import ramana.example.niotcpserver.codec.parser.EitherOfBytes;
import ramana.example.niotcpserver.codec.parser.OneByte;

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
    public static final byte SEMI_COLON = ';';
    public static final byte CR = '\r';
    public static final byte[] CRLF = new byte[] {'\r', '\n'};
    public static final byte[] HTTP_SLASH = "HTTP/".getBytes();
    public static final int REQ_TARGET_MAX_LEN = 512;
    public static final int REQ_FIELD_MAX_LEN = 128;
    public static final int REQ_FIELD_VAL_MAX_LEN = 512;
    public static final int REQ_CHUNK_LEN_MAX_LEN = 32;
    public static final int REQ_CHUNK_EXT_MAX_LEN = 512;
    public static final String REQ_HEADER_CONTENT_LENGTH = "Content-Length";
    public static final String REQ_HEADER_TRANSFER_ENCODING = "Transfer-Encoding";
    public static final String REQ_HEADER_TRANSFER_ENCODING_CHUNKED = "chunked";


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
