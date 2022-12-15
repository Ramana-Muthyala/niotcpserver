package ramana.example.niotcpserver.codec.http.request;

import ramana.example.niotcpserver.codec.http.Util;
import ramana.example.niotcpserver.codec.parser.CompositeParser;
import ramana.example.niotcpserver.codec.parser.DelimiterStringParser;
import ramana.example.niotcpserver.codec.parser.OneByte;

public class LastChunkParser extends CompositeParser<Void> {
    public LastChunkParser() {
        parsers.add(new OneByte((byte) '0'));
        parsers.add(new DelimiterStringParser(Util.CR, Util.CR, Util.REQ_CHUNK_EXT_MAX_LEN, Util.REQ_CHUNK_EXT_MAX_LEN));
        parsers.add(Util.createCRLFParser());
    }

    @Override
    protected Void composeResult() {
        return null;
    }
}
