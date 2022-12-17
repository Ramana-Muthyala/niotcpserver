package ramana.example.niotcpserver.codec.parser;

import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class ZeroOrMore<T, P extends AbstractParser<T>> extends AbstractParser<List<T>> {
    private final Deque<Allocator.Resource<ByteBuffer>> dataDeque = new LinkedList<>();
    private final P parser;

    public ZeroOrMore(P parser) {
        this.parser = parser;
        result = new ArrayList<>();
    }

    @Override
    public void parse(Allocator.Resource<ByteBuffer> data) throws ParseException, InternalException {
        if(status == Status.DONE) return;
        status = Status.IN_PROGRESS;

        ByteBuffer byteBuffer = data.get();
        dataDeque.offer(data);
        while(byteBuffer.hasRemaining()) {
            try {
                parser.parse(data);
                if(parser.status == Status.DONE) {
                    dataDeque.clear();
                    if(byteBuffer.hasRemaining()) dataDeque.offer(data);
                    result.add(parser.result);
                    parser.reset();
                }
            } catch (ParseCompleteSignalException exception) {
                result.add(parser.result);
                status = Status.DONE;
                return;
            } catch (ParseException exception) {
                status = Status.DONE;
                throw new ParseCompletePushBackSignalException(exception, dataDeque.peek());
            }
        }
    }

    @Override
    public void reset() {
        parser.reset();
        dataDeque.clear();
        result = new ArrayList<>();
        status = Status.NONE;
    }
}
