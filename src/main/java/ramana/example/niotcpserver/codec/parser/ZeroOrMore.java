package ramana.example.niotcpserver.codec.parser;

import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ZeroOrMore<T, P extends AbstractParser<T>> extends AbstractPushbackParser<List<T>> {
    private final P parser;

    public ZeroOrMore(P parser) {
        this.parser = parser;
        result = new ArrayList<>();
    }

    @Override
    public void parse(Allocator.Resource<ByteBuffer> data) throws ParseException, InternalException {
        if(status == Status.DONE) return;
        status = Status.IN_PROGRESS;

        dataDeque.offer(data);
        while((data = dataDeque.poll()) != null) {
            ByteBuffer byteBuffer = data.get();
            MarkWrapper markWrapper = new MarkWrapper(data, byteBuffer.position());
            while(byteBuffer.hasRemaining()) {
                try {
                    parser.parse(data);
                    if(parser.status == Status.DONE) {
                        stack.clear();
                        result.add(parser.result);
                        parser.reset();
                        markWrapper.mark(byteBuffer.position());
                    }
                } catch (ParseCompleteSignalException exception) {
                    stack.clear();
                    result.add(parser.result);
                    status = Status.DONE;
                    return;
                } catch (ParseException exception) {
                    status = Status.DONE;
                    return;
                }
            }
            if(parser.status == Status.IN_PROGRESS) stack.offer(markWrapper);
            if(status == Status.DONE) pushBack();
        }
    }

    @Override
    public void reset() {
        parser.reset();
        stack.clear();
        result = new ArrayList<>();
        status = Status.NONE;
    }
}
