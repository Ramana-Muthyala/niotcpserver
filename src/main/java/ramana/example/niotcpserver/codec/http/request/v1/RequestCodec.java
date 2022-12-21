package ramana.example.niotcpserver.codec.http.request.v1;

import ramana.example.niotcpserver.codec.parser.ParseException;
import ramana.example.niotcpserver.codec.parser.v1.AbstractStateParser;
import ramana.example.niotcpserver.codec.parser.v1.Buffer;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

public class RequestCodec {
    private AbstractStateParser parser;
    private final Buffer buffer = new Buffer();
    private final Queue<RequestMessage> messageQueue = new LinkedList<>();
    private RequestMessage requestMessage;

    public RequestCodec() {
        requestMessage = new RequestMessage();
        parser = new MethodParser(requestMessage);
    }

    public void decode(Allocator.Resource<ByteBuffer> data) throws ParseException, InternalException {
        data.get().flip();
        buffer.write(data);
        do {
            parser.parse(buffer);
            if(parser.done) parser = parser.next;
            if(parser == null) {
                messageQueue.offer(requestMessage);
                requestMessage = new RequestMessage();
                parser = new MethodParser(requestMessage);
            }
        } while(buffer.hasRemaining());
    }

    public RequestMessage[] get() {
        RequestMessage[] messageArray = messageQueue.toArray(new RequestMessage[0]);
        messageQueue.clear();
        return messageArray;
    }
}
