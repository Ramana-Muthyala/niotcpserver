package ramana.example.niotcpserver.codec.parser;

import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Deque;

public class EitherOf<T, P extends AbstractParser<T>> extends AbstractPushbackParser<T> {
    private final ArrayList<P> parsers;
    int index;

    public EitherOf(ArrayList<P> parsers, Deque<Allocator.Resource<ByteBuffer>> dataDeque) {
        super(dataDeque);
        this.parsers = parsers;
    }

    @Override
    public void parse(Allocator.Resource<ByteBuffer> data) throws ParseException, InternalException {
        if(status == Status.DONE) return;
        status = Status.IN_PROGRESS;
        ByteBuffer byteBuffer = data.get();
        byteBuffer.mark();
        while(byteBuffer.hasRemaining()) {
            try {
                P parser = parsers.get(index);
                parser.parse(data);
                if(parser.status == Status.DONE) {
                    stack.clear();
                    result = parser.result;
                    status = Status.DONE;
                    return;
                }
                if(stack.peekLast() != data) stack.offer(data);
            } catch (ParseException exception) {
                index++;
                if(stack.peekLast() != data) stack.offer(data);
                pushBack();
                if(index == parsers.size()) throw exception;
            }
        }
    }

    @Override
    protected void reset() {
        index = 0;
        parsers.forEach(AbstractParser::reset);
        super.reset();
    }
}
