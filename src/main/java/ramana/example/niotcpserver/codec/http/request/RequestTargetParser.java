package ramana.example.niotcpserver.codec.http.request;

import ramana.example.niotcpserver.codec.http.Util;
import ramana.example.niotcpserver.codec.parser.AbstractParser;
import ramana.example.niotcpserver.codec.parser.DelimiterStringParser;
import ramana.example.niotcpserver.codec.parser.ParseException;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;

public class RequestTargetParser extends AbstractParser<RequestTarget> {
    private final DelimiterStringParser parser;

    public RequestTargetParser() {
        /*
        * Instead of delegating to DelimiterParser, we can directly parse by operating on ByteBuffer
        * and that works faster.
        * As it is just an example, I am using DelimiterParser.
        * Can be changed anytime.
        * */
        parser = new DelimiterStringParser(Util.SP, Util.CR, Util.REQ_TARGET_MAX_LEN, Util.REQ_TARGET_MAX_LEN);
    }

    @Override
    public void parse(Allocator.Resource<ByteBuffer> data) throws ParseException, InternalException {
        if(status == Status.DONE) return;
        status = Status.IN_PROGRESS;
        parser.parse(data);
        if(parser.getStatus() == Status.DONE) {
            result = new RequestTarget(parser.getResult());
            status = Status.DONE;
        }
    }
}
