package ramana.example.niotcpserver.codec.http.request;

import ramana.example.niotcpserver.codec.parser.ParseException;
import ramana.example.niotcpserver.codec.parser.Util;
import ramana.example.niotcpserver.codec.parser.ZeroOrMore;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.List;

public class HeadersParser extends ZeroOrMore<Header, HeaderParser> {
    private final RequestParser requestParser;

    public HeadersParser(HeaderParser parser, Deque<Allocator.Resource<ByteBuffer>> dataDeque, RequestParser requestParser) {
        super(parser, dataDeque);
        this.requestParser = requestParser;
    }

    @Override
    public void parse(Allocator.Resource<ByteBuffer> data) throws ParseException, InternalException {
        super.parse(data);
        if(status == Status.DONE) {
            int contentLength = checkContentLengthHeader(result);
            if(contentLength > 0) requestParser.add(new ContentLengthBasedBodyParser(contentLength));
        }
    }

    private int checkContentLengthHeader(List<Header> headers) throws ParseException {
        for (Header header: headers) {
            if(Util.REQ_HEADER_CONTENT_LENGTH.equals(header.name)) {
                try {
                    return Integer.parseInt(header.values.get(0));
                } catch (IndexOutOfBoundsException | NumberFormatException exception) {
                    throw new ParseException(exception);
                }
            }
        }
        return 0;
    }
}
