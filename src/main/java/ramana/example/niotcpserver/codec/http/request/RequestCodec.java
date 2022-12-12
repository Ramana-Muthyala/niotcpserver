package ramana.example.niotcpserver.codec.http.request;

import ramana.example.niotcpserver.codec.parser.AbstractParser;
import ramana.example.niotcpserver.codec.parser.ParseException;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.LinkedList;

/*
 * HTTP/1.1 - ref: https://httpwg.org/specs/rfc9112.html
 * RequestCodec refers to the above link but is implemented in a very lenient manner.
 * Provisions are given to aid incremental evolution.
 * Focus is on providing a parsing framework and a simple example.
 */
public class RequestCodec {
    private final AbstractParser<RequestMessage> parser;
    private final Deque<ByteBuffer> dataDeque = new LinkedList<>();

    public RequestCodec() {
        parser = new RequestParser(dataDeque);
    }

    public void decode(Allocator.Resource<ByteBuffer> data) throws ParseException, InternalException {
        ByteBuffer byteBuffer = data.get();
        byteBuffer.flip();
        parser.parse(byteBuffer);
        while((byteBuffer = dataDeque.poll()) != null  &&  byteBuffer.hasRemaining()) {
            parser.parse(byteBuffer);
        }
    }

    public RequestMessage get() {
        return parser.getResult();
    }

    public boolean isDecoded() {
        return parser.getStatus() == AbstractParser.Status.DONE;
    }
}
