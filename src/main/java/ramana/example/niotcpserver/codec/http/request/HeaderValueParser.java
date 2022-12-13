package ramana.example.niotcpserver.codec.http.request;

import ramana.example.niotcpserver.codec.parser.*;

import java.nio.ByteBuffer;

public class HeaderValueParser extends AbstractParser<String> {
    private final DelimiterParser parser;

    public HeaderValueParser() {
        parser = new DelimiterParser(Util.COMMA, Util.CR, Util.REQ_HEADER_VAL_MAX_LEN, Util.REQ_HEADER_VAL_MAX_LEN);
    }

    @Override
    public void parse(ByteBuffer data) throws ParseException {
        if(status == Status.DONE) return;
        status = Status.IN_PROGRESS;
        try {
            parser.parse(data);
        } catch (DelimiterBreakPointParseException exception) {
            status = Status.DONE;
            result = parser.getResult();
            throw new ParseCompleteSignalException(exception);
        }
        if(parser.getStatus() == Status.DONE) {
            data.position(data.position() + 1);
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
