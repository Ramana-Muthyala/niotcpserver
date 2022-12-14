package ramana.example.niotcpserver.codec.http.request;

import ramana.example.niotcpserver.codec.parser.AbstractParser;
import ramana.example.niotcpserver.codec.parser.FixedLengthParser;
import ramana.example.niotcpserver.codec.parser.ParseException;
import ramana.example.niotcpserver.codec.http.Util;
import ramana.example.niotcpserver.codec.parser.ZeroOrMore;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class HeadersParser extends ZeroOrMore<Field, FieldLineParser> {
    private final RequestParser requestParser;

    public HeadersParser(FieldLineParser parser, Deque<Allocator.Resource<ByteBuffer>> dataDeque, RequestParser requestParser) {
        super(parser, dataDeque);
        this.requestParser = requestParser;
    }

    @Override
    public void parse(Allocator.Resource<ByteBuffer> data) throws ParseException, InternalException {
        super.parse(data);
        if(status == Status.DONE) {
            AbstractParser bodyParser = checkBodyProcessorHeaders(result);
            if(bodyParser != null) requestParser.add(bodyParser);
        }
    }

    private AbstractParser checkBodyProcessorHeaders(List<Field> headers) throws ParseException {
        Integer contentLength = null;
        ArrayList<String> transferEncoding = null;
        for (Field header: headers) {
            if(Util.REQ_HEADER_CONTENT_LENGTH.equals(header.name)) {
                try {
                    contentLength = Integer.parseInt(header.values.get(0));
                } catch (IndexOutOfBoundsException | NumberFormatException exception) {
                    throw new ParseException(exception);
                }
            } else if(Util.REQ_HEADER_TRANSFER_ENCODING.equals(header.name)) {
                transferEncoding = header.values;
            }
            if(contentLength != null  &&  transferEncoding != null) throw new ParseException();
        }
        if(transferEncoding != null  &&  transferEncoding.contains(Util.REQ_HEADER_TRANSFER_ENCODING_CHUNKED)) {
            return new ChunkedBodyParser(dataDeque, result);
        }
        if(contentLength != null) {
            if(contentLength < 0) throw new ParseException();
            if(contentLength > 0) return new FixedLengthParser(contentLength);
        }
        return null;
    }
}
