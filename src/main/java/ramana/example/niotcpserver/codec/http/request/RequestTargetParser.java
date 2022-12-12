package ramana.example.niotcpserver.codec.http.request;

import ramana.example.niotcpserver.codec.parser.AbstractParser;
import ramana.example.niotcpserver.codec.parser.DelimiterParser;
import ramana.example.niotcpserver.codec.parser.ParseException;
import ramana.example.niotcpserver.codec.parser.Util;

import java.nio.ByteBuffer;

public class RequestTargetParser extends AbstractParser<RequestTarget> {
    private final DelimiterParser parser;

    public RequestTargetParser() {
        /*
        * Instead of delegating to DelimiterParser, we can directly parse by operating on ByteBuffer
        * and that works faster.
        * As it is just an example, I am using DelimiterParser.
        * Can be changed anytime.
        * */
        parser = new DelimiterParser(Util.SP, Util.CR, 512, 512);
    }

    @Override
    public void parse(ByteBuffer data) throws ParseException {
        if(status == Status.DONE) return;
        status = Status.IN_PROGRESS;
        parser.parse(data);
        if(parser.getStatus() == Status.DONE) {
            result = new RequestTarget(parser.getResult());
            status = Status.DONE;
        }
    }
}
