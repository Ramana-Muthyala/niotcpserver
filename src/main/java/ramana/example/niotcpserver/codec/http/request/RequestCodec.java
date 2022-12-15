package ramana.example.niotcpserver.codec.http.request;

import ramana.example.niotcpserver.codec.parser.AbstractParser;
import ramana.example.niotcpserver.codec.parser.ParseException;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

/*
 * HTTP/1.1 - ref: https://httpwg.org/specs/rfc9112.html
 * RequestCodec refers to the above link but is implemented in a very lenient manner.
 * Provisions are given to aid incremental evolution.
 * Focus is on providing a parsing framework and a simple example.
 */
public class RequestCodec {
    private AbstractParser<RequestMessage> parser;
    private final Queue<RequestMessage> messageQueue = new LinkedList<>();

    public RequestCodec() {
        parser = new RequestParser();
    }

    public void decode(Allocator.Resource<ByteBuffer> data) throws InternalException, ParseException {
        ByteBuffer byteBuffer = data.get();
        byteBuffer.flip();
        while(byteBuffer.hasRemaining()) {
            parser.parse(data);
            if(parser.getStatus() == AbstractParser.Status.DONE) {
                messageQueue.offer(parser.getResult());
                parser = new RequestParser();
            }
        }
    }

    public RequestMessage[] get() {
        RequestMessage[] messageArray = messageQueue.toArray(new RequestMessage[0]);
        messageQueue.clear();
        return messageArray;
    }
}
