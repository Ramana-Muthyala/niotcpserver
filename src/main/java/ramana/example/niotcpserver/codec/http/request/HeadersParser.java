package ramana.example.niotcpserver.codec.http.request;

import ramana.example.niotcpserver.codec.http.Util;
import ramana.example.niotcpserver.codec.parser.*;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;

public class HeadersParser extends ZeroOrMore<Field, FieldLineParser> {
    private final RequestParser requestParser;
    Map<String, ArrayList<String>> headers;

    public HeadersParser(FieldLineParser parser, RequestParser requestParser) {
        super(parser);
        this.requestParser = requestParser;
    }

    @Override
    public void parse(Allocator.Resource<ByteBuffer> data) throws ParseException, InternalException {
        ParseCompletePushBackSignalException pushBackSignal = null;
        try {
            super.parse(data);
        } catch (ParseCompletePushBackSignalException exception) {
            pushBackSignal = exception;
        }

        if(status == Status.DONE) {
            headers = Util.toMap(result);
            AbstractParser bodyParser = checkBodyProcessorHeaders();
            if(bodyParser != null) requestParser.add(bodyParser);
            if(pushBackSignal != null) throw pushBackSignal;
        }
    }

    private AbstractParser checkBodyProcessorHeaders() throws ParseException {
        ArrayList<String> contentLength = headers.get(Util.REQ_HEADER_CONTENT_LENGTH);
        ArrayList<String> transferEncoding = headers.get(Util.REQ_HEADER_TRANSFER_ENCODING);
        if(contentLength != null  &&  transferEncoding != null) throw new ParseException();
        if(transferEncoding != null  &&  transferEncoding.contains(Util.REQ_HEADER_TRANSFER_ENCODING_CHUNKED)) {
            return new ChunkedBodyParser(headers);
        }
        if(contentLength != null) {
            try {
                int contentLengthInt = Integer.parseInt(contentLength.get(0));
                if(contentLengthInt < 0) throw new ParseException();
                if(contentLengthInt > 0) return new FixedLengthParser(contentLengthInt);
            } catch (IndexOutOfBoundsException | NumberFormatException exception) {
                throw new ParseException(exception);
            }
        }
        return null;
    }
}
