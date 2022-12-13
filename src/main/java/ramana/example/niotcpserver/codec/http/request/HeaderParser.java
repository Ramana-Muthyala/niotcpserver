package ramana.example.niotcpserver.codec.http.request;

import ramana.example.niotcpserver.codec.parser.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Deque;

public class HeaderParser extends CompositeParser<Header> {
    public HeaderParser(Deque<ByteBuffer> dataDeque) {
        super(dataDeque);
        parsers.add(new DelimiterParser(Util.COLON, Util.CR, Util.REQ_HEADER_MAX_LEN, Util.REQ_HEADER_MAX_LEN));
        parsers.add(new OneByte(Util.COLON));
        parsers.add(new OneOrMore(new HeaderValueParser(), dataDeque));
        parsers.add(Util.createCRLFParser());
    }

    @Override
    protected Header composeResult() {
        return new Header((String) parsers.get(0).getResult(), (ArrayList<String>)parsers.get(2).getResult());
    }

    @Override
    protected void reset() {
        parsers.clear();
        parsers.add(new DelimiterParser(Util.COLON, Util.CR, Util.REQ_HEADER_MAX_LEN, Util.REQ_HEADER_MAX_LEN));
        parsers.add(new OneByte(Util.COLON));
        parsers.add(new OneOrMore(new HeaderValueParser(), dataDeque));
        parsers.add(Util.createCRLFParser());
        super.reset();
    }
}
