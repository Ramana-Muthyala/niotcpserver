package ramana.example.niotcpserver.codec.http.request;

import ramana.example.niotcpserver.codec.parser.CompositeParser;
import ramana.example.niotcpserver.codec.parser.ParseException;
import ramana.example.niotcpserver.codec.parser.Util;
import ramana.example.niotcpserver.codec.parser.ZeroOrMore;

import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.List;

public class RequestParser extends CompositeParser<RequestMessage> {
    public RequestParser(Deque<ByteBuffer> dataDeque) {
        super(dataDeque);
        parsers.add(new RequestLineParser(dataDeque));
        parsers.add(new ZeroOrMore(new HeaderParser(dataDeque), dataDeque));
        parsers.add(Util.createCRLFParser());
    }

    @Override
    public void parse(ByteBuffer data) throws ParseException {
        super.parse(data);
        // after reading headers, parse request content here.
    }

    @Override
    protected RequestMessage composeResult() {
        return new RequestMessage((RequestLine)parsers.get(0).getResult(), (List<Header>)parsers.get(1).getResult());
    }
}
