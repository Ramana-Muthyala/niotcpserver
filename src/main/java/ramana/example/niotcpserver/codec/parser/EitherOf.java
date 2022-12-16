package ramana.example.niotcpserver.codec.parser;

import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class EitherOf<T, P extends AbstractParser<T>> extends AbstractPushbackParser<T> {
    private final ArrayList<P> parsers;
    int index;

    public EitherOf(ArrayList<P> parsers) {
        this.parsers = parsers;
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
                    P parser = parsers.get(index);
                    parser.parse(data);
                    if(parser.status == Status.DONE) {
                        stack.clear();
                        result = parser.result;
                        status = Status.DONE;
                        return;
                    }
                    stack.offer(markWrapper);
                } catch (ParseException exception) {
                    index++;
                    if(index == parsers.size()) throw exception;
                    stack.offer(markWrapper);
                    pushBack();
                    break;
                }
            }
        }
    }

    @Override
    public void reset() {
        index = 0;
        parsers.forEach(AbstractParser::reset);
        super.reset();
    }
}
