package ramana.example.niotcpserver.codec.parser;

import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Deque;

public abstract class CompositeParser<T> extends AbstractParser<T> {
    protected final Deque<Allocator.Resource<ByteBuffer>> dataDeque;
    protected ArrayList<AbstractParser> parsers = new ArrayList<>();
    protected int index;

    public CompositeParser(Deque<Allocator.Resource<ByteBuffer>> dataDeque) {
        this.dataDeque = dataDeque;
    }

    public CompositeParser<T> add(AbstractParser parser) {
        parsers.add(parser);
        return this;
    }

    @Override
    public void parse(Allocator.Resource<ByteBuffer> data) throws ParseException, InternalException {
        if(status == Status.DONE) return;
        status = Status.IN_PROGRESS;
        ByteBuffer byteBuffer = data.get();
        while(byteBuffer.hasRemaining()) {
            AbstractParser parser = parsers.get(index);
            parser.parse(data);
            if(parser.status == Status.DONE) {
                index++;
                if(index == parsers.size()) {
                    result = composeResult();
                    status = Status.DONE;
                    return;
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
