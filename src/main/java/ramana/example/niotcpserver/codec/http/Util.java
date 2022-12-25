package ramana.example.niotcpserver.codec.http;

import ramana.example.niotcpserver.codec.parser.ByteSequence;
import ramana.example.niotcpserver.codec.parser.ByteRange;
import ramana.example.niotcpserver.codec.parser.OneByte;

import java.util.HashMap;

public class Util {
    public static final int STATUS_OK = 200;
    public static final int STATUS_BAD_REQUEST = 400;
    public static final int STATUS_NOT_FOUND = 404;
    public static final int STATUS_METHOD_NOT_ALLOWED = 405;
    public static final int STATUS_INTERNAL_SERVER_ERROR = 500;
    public static final int STATUS_NOT_IMPLEMENTED = 501;
    public static final HashMap<Integer, String> statusCodeToText = new HashMap<>();

    static {
        statusCodeToText.put(STATUS_OK, "HTTP/1.1 200 OK");
        statusCodeToText.put(STATUS_BAD_REQUEST, "HTTP/1.1 400 Bad Request");
        statusCodeToText.put(STATUS_NOT_FOUND, "HTTP/1.1 404 Not Found");
        statusCodeToText.put(STATUS_METHOD_NOT_ALLOWED, "HTTP/1.1 405 Method Not Allowed");
        statusCodeToText.put(STATUS_INTERNAL_SERVER_ERROR, "HTTP/1.1 500 Internal Server Error");
        statusCodeToText.put(STATUS_NOT_IMPLEMENTED, "HTTP/1.1 501 Not Implemented");
    }
    public static final String METHOD_CONNECT = "CONNECT";
    public static final String METHOD_GET = "GET";
    public static final String METHOD_HEAD = "HEAD";
    public static final String METHOD_OPTIONS = "OPTIONS";
    public static final byte[] GET = "GET".getBytes();
    public static final byte[] HEAD = "HEAD".getBytes();
    public static final byte[] POST = "POST".getBytes();
    public static final byte[] PUT = "PUT".getBytes();
    public static final byte[] DELETE = "DELETE".getBytes();
    public static final byte[] CONNECT = "CONNECT".getBytes();
    public static final byte[] OPTIONS = "OPTIONS".getBytes();
    public static final byte[] TRACE = "TRACE".getBytes();
    public static final byte ZERO = '0';
    public static final byte NINE = '9';
    public static final byte SP = ' ';
    public static final byte QUESTION_MARK = '?';
    public static final byte EQUAL_TO = '=';
    public static final byte AMPERSAND = '&';
    public static final byte DOT = '.';
    public static final byte COMMA = ',';
    public static final byte COLON = ':';
    public static final byte SEMI_COLON = ';';
    public static final byte SLASH = '/';
    public static final byte CR = '\r';
    public static final byte LF = '\n';
    public static final byte[] CRLF = new byte[] {'\r', '\n'};
    public static final String CRLF_STRING = "\r\n";
    public static final byte[] HTTP_SLASH = "HTTP/".getBytes();

    // ABSOLUTE_FORM_SCHEMA is http:// but h would have been already consumed while parsing.
    public static final byte[] ABSOLUTE_FORM_SCHEMA = "ttp://".getBytes();
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

    public static ByteRange createDigitParser() {
        return new ByteRange((byte) '0', (byte) '9');
    }

    public static OneByte createDotParser() {
        return new OneByte(DOT);
    }

    public static ByteSequence createCRLFParser() {
        return new ByteSequence(CRLF);
    }
}
