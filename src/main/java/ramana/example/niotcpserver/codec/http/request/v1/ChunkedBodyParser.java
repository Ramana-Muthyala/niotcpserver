package ramana.example.niotcpserver.codec.http.request.v1;

import ramana.example.niotcpserver.codec.parser.ParseException;
import ramana.example.niotcpserver.codec.parser.v1.AbstractStateParser;
import ramana.example.niotcpserver.codec.parser.v1.Buffer;
import ramana.example.niotcpserver.types.InternalException;

public class ChunkedBodyParser extends AbstractStateParser {
    private final RequestMessage requestMessage;

    public ChunkedBodyParser(RequestMessage requestMessage) {
        this.requestMessage = requestMessage;
    }

    @Override
    public void parse(Buffer buffer) throws ParseException, InternalException {

    }
}
