package ramana.example.niotcpserver.codec.http.request;

import ramana.example.niotcpserver.codec.http.Util;
import ramana.example.niotcpserver.codec.parser.CompositeParser;

public class RequestParser extends CompositeParser<RequestMessage> {
    public RequestParser() {
        parsers.add(new RequestLineParser());
        parsers.add(new HeadersParser(new FieldLineParser(), this));
        parsers.add(Util.createCRLFParser());
    }

    @Override
    protected RequestMessage composeResult() {
        byte[] body = (this.parsers.size() == 4) ? (byte[]) parsers.get(3).getResult() : null;
        return new RequestMessage((RequestLine)parsers.get(0).getResult(), ((HeadersParser)parsers.get(1)).headers, body);
    }
}
