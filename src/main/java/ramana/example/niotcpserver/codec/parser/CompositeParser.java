package ramana.example.niotcpserver.codec.parser;

import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public abstract class CompositeParser<T> extends AbstractPushbackParser<T> {
    protected ArrayList<AbstractParser> parsers = new ArrayList<>();
    protected int index;

    public CompositeParser<T> add(AbstractParser parser) {
        parsers.add(parser);
        return this;
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
                AbstractParser parser = parsers.get(index);
                try {
                    parser.parse(data);
                    if(parser.status == Status.DONE) {
                        stack.clear();
                        markWrapper.mark(byteBuffer.position());
                        index++;
                        if(index == parsers.size()) {
                            result = composeResult();
                            status = Status.DONE;
                            return;
                        }
                    } else {
                        stack.offer(markWrapper);
                    }
                } catch (ParseCompletePushBackSignalException exception) {
                    stack.offer(markWrapper);
                    pushBack(exception.pushBackPoint);
                    index++;
                    if(index == parsers.size()) {
                        result = composeResult();
                        status = Status.DONE;
                        return;
                    }
                    break;
                }
            }
        }
    }

    protected abstract T composeResult();

    @Override
    public void reset() {
        index = 0;
        parsers.forEach(AbstractParser::reset);
        super.reset();
    }
}
