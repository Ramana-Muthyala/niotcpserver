package ramana.example.niotcpserver.codec.http.request;

import ramana.example.niotcpserver.codec.parser.CompositeParser;
import ramana.example.niotcpserver.codec.parser.DelimiterParser;
import ramana.example.niotcpserver.codec.parser.OneByte;
import ramana.example.niotcpserver.codec.parser.Util;

import java.nio.ByteBuffer;
import java.util.Deque;

public class HeaderParser extends CompositeParser<Header> {
    public HeaderParser(Deque<ByteBuffer> dataDeque) {
        super(dataDeque);
        parsers.add(new DelimiterParser(Util.COLON, 128, 128));
        parsers.add(new OneByte(Util.COLON));
        parsers.add(new DelimiterParser(Util.CR, 512, 512));
        parsers.add(Util.createCRLFParser());
    }

    @Override
    protected Header composeResult() {
        return new Header((String) parsers.get(0).getResult(), (String) parsers.get(2).getResult());
    }
}
