package ramana.example.niotcpserver.codec.http.request;

import ramana.example.niotcpserver.codec.http.Util;
import ramana.example.niotcpserver.codec.parser.CompositeParser;
import ramana.example.niotcpserver.codec.parser.DelimiterStringParser;
import ramana.example.niotcpserver.codec.parser.OneByte;
import ramana.example.niotcpserver.codec.parser.OneOrMore;

import java.util.ArrayList;

public class FieldLineParser extends CompositeParser<Field> {
    public FieldLineParser() {
        parsers.add(new DelimiterStringParser(Util.COLON, Util.CR, Util.REQ_FIELD_MAX_LEN, Util.REQ_FIELD_MAX_LEN));
        parsers.add(new OneByte(Util.COLON));
        parsers.add(new OneOrMore<>(new FieldValueParser()));
        parsers.add(Util.createCRLFParser());
    }

    @Override
    protected Field composeResult() {
        return new Field((String) parsers.get(0).getResult(), (ArrayList<String>)parsers.get(2).getResult());
    }
}
