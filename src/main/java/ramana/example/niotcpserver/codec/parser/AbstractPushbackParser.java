package ramana.example.niotcpserver.codec.parser;

import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.LinkedList;

public abstract class AbstractPushbackParser<T> extends AbstractParser<T> {
    protected Deque<ByteBuffer> stack = new LinkedList<>();
    protected final Deque<ByteBuffer> dataDeque;

    protected AbstractPushbackParser(Deque<ByteBuffer> dataDeque) {
        this.dataDeque = dataDeque;
    }

    protected void pushBack() {
        ByteBuffer tmp;
        while((tmp = stack.pollLast()) != null) {
            tmp.reset();
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
