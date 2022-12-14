package ramana.example.niotcpserver.codec.http.request;

import ramana.example.niotcpserver.codec.parser.*;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;

public class HeaderValueParser extends AbstractParser<String> {
    private final DelimiterParser parser;

    public HeaderValueParser() {
        parser = new DelimiterParser(Util.COMMA, Util.CR, Util.REQ_HEADER_VAL_MAX_LEN, Util.REQ_HEADER_VAL_MAX_LEN);
    }

    @Override
    public void parse(Allocator.Resource<ByteBuffer> data) throws ParseException, InternalException {
        if(status == Status.DONE) return;
        status = Status.IN_PROGRESS;
        ByteBuffer byteBuffer = data.get();
        try {
            parser.parse(data);
        } catch (DelimiterBreakPointParseException exception) {
            status = Status.DONE;
            result = parser.getResult();
            throw new ParseCompleteSignalException(exception);
        }
        if(parser.getStatus() == Status.DONE) {
            byteBuffer.position(byteBuffer.position() + 1);
            status = Status.DONE;
            result = parser.getResult();
        }
    }

    @Override
    protected void reset() {
        parser.reset();
        super.reset();
    }
}
