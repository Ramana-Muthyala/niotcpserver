package ramana.example.niotcpserver.codec.http.request;

import ramana.example.niotcpserver.codec.parser.*;
import ramana.example.niotcpserver.io.Allocator;

import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.List;

public class RequestParser extends CompositeParser<RequestMessage> {
    public RequestParser(Deque<Allocator.Resource<ByteBuffer>> dataDeque) {
        super(dataDeque);
        parsers.add(new RequestLineParser(dataDeque));
        parsers.add(new HeadersParser(new HeaderParser(dataDeque), dataDeque, this));
        parsers.add(Util.createCRLFParser());
    }

    @Override
    protected RequestMessage composeResult() {
        byte[] body = (this.parsers.size() == 4) ? (byte[]) parsers.get(3).getResult() : null;
        return new RequestMessage((RequestLine)parsers.get(0).getResult(), (List<Header>)parsers.get(1).getResult(), body);
    }
}
