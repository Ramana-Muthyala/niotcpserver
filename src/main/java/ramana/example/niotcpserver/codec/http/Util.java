package ramana.example.niotcpserver.codec.http;

import ramana.example.niotcpserver.codec.parser.ByteSequence;
import ramana.example.niotcpserver.codec.parser.ByteRange;
import ramana.example.niotcpserver.codec.parser.OneByte;

import java.util.HashMap;

public class Util {
    public static final int STATUS_CONTINUE = 100;
    public static final int STATUS_OK = 200;
    public static final int STATUS_CREATED = 201;
    public static final int STATUS_ACCEPTED = 202;
    public static final int STATUS_NON_AUTHORITATIVE_INFORMATION = 203;
    public static final int STATUS_NO_CONTENT = 204;
    public static final int STATUS_RESET_CONTENT = 205;
    public static final int STATUS_PARTIAL_CONTENT = 206;
    public static final int STATUS_MOVED_PERMANENTLY = 301;
    public static final int STATUS_TEMPORARY_REDIRECT = 307;
    public static final int STATUS_BAD_REQUEST = 400;
    public static final int STATUS_UNAUTHORIZED = 401;
    public static final int STATUS_PAYMENT_REQUIRED = 402;
    public static final int STATUS_FORBIDDEN = 403;
    public static final int STATUS_NOT_FOUND = 404;
    public static final int STATUS_METHOD_NOT_ALLOWED = 405;
    public static final int STATUS_NOT_ACCEPTABLE = 406;
    public static final int STATUS_PROXY_AUTHENTICATION_REQUIRED = 407;
    public static final int STATUS_REQUEST_TIMEOUT = 408;
    public static final int STATUS_CONFLICT = 409;
    public static final int STATUS_GONE = 410;
    public static final int STATUS_LENGTH_REQUIRED = 411;
    public static final int STATUS_REQUEST_URI_TOO_LONG = 414;
    public static final int STATUS_UNSUPPORTED_MEDIA_TYPE = 415;
    public static final int STATUS_INTERNAL_SERVER_ERROR = 500;
    public static final int STATUS_NOT_IMPLEMENTED = 501;
    public static final int STATUS_SERVICE_UNAVAILABLE = 503;
    public static final HashMap<Integer, String> statusCodeToText = new HashMap<>();

    static {
        statusCodeToText.put(STATUS_CONTINUE, "HTTP/1.1 100 Continue");
        statusCodeToText.put(STATUS_OK, "HTTP/1.1 200 OK");
        statusCodeToText.put(STATUS_CREATED, "HTTP/1.1 201 Created");
        statusCodeToText.put(STATUS_ACCEPTED, "HTTP/1.1 202 Accepted");
        statusCodeToText.put(STATUS_NON_AUTHORITATIVE_INFORMATION, "HTTP/1.1 203 Non-Authoritative Information");
        statusCodeToText.put(STATUS_NO_CONTENT, "HTTP/1.1 204 No Content");
        statusCodeToText.put(STATUS_RESET_CONTENT, "HTTP/1.1 205 Reset Content");
        statusCodeToText.put(STATUS_PARTIAL_CONTENT, "HTTP/1.1 206 Partial Content");
        statusCodeToText.put(STATUS_MOVED_PERMANENTLY, "HTTP/1.1 301 Moved Permanently");
        statusCodeToText.put(STATUS_TEMPORARY_REDIRECT, "HTTP/1.1 307 Temporary Redirect");
        statusCodeToText.put(STATUS_BAD_REQUEST, "HTTP/1.1 400 Bad Request");
        statusCodeToText.put(STATUS_UNAUTHORIZED, "HTTP/1.1 401 Unauthorized");
        statusCodeToText.put(STATUS_PAYMENT_REQUIRED, "HTTP/1.1 402 Payment Required");
        statusCodeToText.put(STATUS_FORBIDDEN, "HTTP/1.1 403 Forbidden");
        statusCodeToText.put(STATUS_NOT_FOUND, "HTTP/1.1 404 Not Found");
        statusCodeToText.put(STATUS_METHOD_NOT_ALLOWED, "HTTP/1.1 405 Method Not Allowed");
        statusCodeToText.put(STATUS_NOT_ACCEPTABLE, "HTTP/1.1 406 Not Acceptable");
        statusCodeToText.put(STATUS_PROXY_AUTHENTICATION_REQUIRED, "HTTP/1.1 407 Proxy Authentication Required");
        statusCodeToText.put(STATUS_REQUEST_TIMEOUT, "HTTP/1.1 408 Request Timeout");
        statusCodeToText.put(STATUS_CONFLICT, "HTTP/1.1 409 Conflict");
        statusCodeToText.put(STATUS_GONE, "HTTP/1.1 410 Gone");
        statusCodeToText.put(STATUS_LENGTH_REQUIRED, "HTTP/1.1 411 Length Required");
        statusCodeToText.put(STATUS_REQUEST_URI_TOO_LONG, "HTTP/1.1 414 Request-URI Too Long");
        statusCodeToText.put(STATUS_UNSUPPORTED_MEDIA_TYPE, "HTTP/1.1 415 Unsupported Media Type");
        statusCodeToText.put(STATUS_INTERNAL_SERVER_ERROR, "HTTP/1.1 500 Internal Server Error");
        statusCodeToText.put(STATUS_NOT_IMPLEMENTED, "HTTP/1.1 501 Not Implemented");
        statusCodeToText.put(STATUS_SERVICE_UNAVAILABLE, "HTTP/1.1 503 Service Unavailable");
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
    public static final byte[] PATCH = "PATCH".getBytes();
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
