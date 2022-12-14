package ramana.example.niotcpserver.codec.parser;

import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class ZeroOrMore<T, P extends AbstractParser<T>> extends AbstractPushbackParser<List<T>> {
    private final P parser;

    public ZeroOrMore(P parser, Deque<Allocator.Resource<ByteBuffer>> dataDeque) {
        super(dataDeque);
        this.parser = parser;
        result = new ArrayList<>();
    }

    @Override
    public void parse(Allocator.Resource<ByteBuffer> data) throws ParseException, InternalException {
        if(status == Status.DONE) return;
        status = Status.IN_PROGRESS;
        ByteBuffer byteBuffer = data.get();
        byteBuffer.mark();
        while(byteBuffer.hasRemaining()) {
            try {
                parser.parse(data);
                if(parser.status == Status.DONE) {
                    stack.clear();
                    result.add(parser.result);
                    parser.reset();
                    byteBuffer.mark();
                }
            } catch (ParseCompleteSignalException exception) {
                stack.clear();
                result.add(parser.result);
                status = Status.DONE;
                break;
            } catch (ParseException exception) {
                status = Status.DONE;
                break;
            }
        }
        if(parser.status == Status.IN_PROGRESS) stack.offer(data);
        if(status == Status.DONE) pushBack();
    }

    @Override
    protected void reset() {
        parser.reset();
        stack.clear();
        result = new ArrayList<>();
        status = Status.NONE;
    }
}
