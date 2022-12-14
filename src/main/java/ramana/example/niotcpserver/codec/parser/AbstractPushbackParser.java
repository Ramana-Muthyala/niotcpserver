package ramana.example.niotcpserver.codec.parser;

import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.LinkedList;

public abstract class AbstractPushbackParser<T> extends AbstractParser<T> {
    protected Deque<Allocator.Resource<ByteBuffer>> stack = new LinkedList<>();
    protected final Deque<Allocator.Resource<ByteBuffer>> dataDeque;

    protected AbstractPushbackParser(Deque<Allocator.Resource<ByteBuffer>> dataDeque) {
        this.dataDeque = dataDeque;
    }

    protected void pushBack() throws InternalException {
        Allocator.Resource<ByteBuffer> tmp;
        while((tmp = stack.pollLast()) != null) {
            ByteBuffer byteBuffer = tmp.get();
            byteBuffer.reset();
            if(tmp == dataDeque.peekFirst()) continue;
            dataDeque.offerFirst(tmp);
        }
    }

    @Override
    protected void reset() {
        stack.clear();
        super.reset();
    }
}
