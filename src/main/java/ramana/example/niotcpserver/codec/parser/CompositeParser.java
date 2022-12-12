package ramana.example.niotcpserver.codec.parser;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Deque;

public abstract class CompositeParser<T> extends AbstractParser<T> {
    protected final Deque<ByteBuffer> dataDeque;
    protected ArrayList<AbstractParser> parsers = new ArrayList<>();
    protected int index;

    public CompositeParser(Deque<ByteBuffer> dataDeque) {
        this.dataDeque = dataDeque;
    }

    public CompositeParser<T> add(AbstractParser parser) {
        parsers.add(parser);
        return this;
    }

    @Override
    public void parse(ByteBuffer data) throws ParseException {
        if(status == Status.DONE) return;
        status = Status.IN_PROGRESS;
        while(data.hasRemaining()) {
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
}
