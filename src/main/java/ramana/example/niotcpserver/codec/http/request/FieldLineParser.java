package ramana.example.niotcpserver.codec.http.request;

import ramana.example.niotcpserver.codec.http.Util;
import ramana.example.niotcpserver.codec.parser.*;
import ramana.example.niotcpserver.io.Allocator;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Deque;

public class FieldLineParser extends CompositeParser<Field> {
    public FieldLineParser(Deque<Allocator.Resource<ByteBuffer>> dataDeque) {
        super(dataDeque);
        parsers.add(new DelimiterStringParser(Util.COLON, Util.CR, Util.REQ_FIELD_MAX_LEN, Util.REQ_FIELD_MAX_LEN));
        parsers.add(new OneByte(Util.COLON));
        parsers.add(new OneOrMore<>(new FieldValueParser(), dataDeque));
        parsers.add(Util.createCRLFParser());
    }

    @Override
    protected Field composeResult() {
        return new Field((String) parsers.get(0).getResult(), (ArrayList<String>)parsers.get(2).getResult());
    }

    @Override
    public void reset() {
        parsers.clear();
        parsers.add(new DelimiterStringParser(Util.COLON, Util.CR, Util.REQ_FIELD_MAX_LEN, Util.REQ_FIELD_MAX_LEN));
        parsers.add(new OneByte(Util.COLON));
        parsers.add(new OneOrMore<>(new FieldValueParser(), dataDeque));
        parsers.add(Util.createCRLFParser());
        super.reset();
    }
}
