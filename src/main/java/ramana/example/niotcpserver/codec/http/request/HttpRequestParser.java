package ramana.example.niotcpserver.codec.http.request;

import ramana.example.niotcpserver.codec.parser.AbstractParser;
import ramana.example.niotcpserver.codec.parser.CompositeParser;

public class HttpRequestParser extends CompositeParser<HttpRequestMessage> {
    public HttpRequestParser(AbstractParser parser) {
        super(parser);
    }

    @Override
    protected HttpRequestMessage composeResult() {
        return null;
    }
}
