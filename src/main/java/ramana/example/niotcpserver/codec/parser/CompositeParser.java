package ramana.example.niotcpserver.codec.parser;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public abstract class CompositeParser<T> extends AbstractParser<T> {
    protected ArrayList<AbstractParser> parsers = new ArrayList<>();
    protected int index;

    public CompositeParser(AbstractParser parser) {
        parsers.add(parser);
    }

    public CompositeParser<T> add(AbstractParser parser) {
        parsers.add(parser);
        return this;
    }

    @Override
    public void parse(ByteBuffer data) throws ParseException {
        status = Status.IN_PROGRESS;
        while(data.hasRemaining()) {
            AbstractParser parser = parsers.get(index);
            if(parser.status == Status.DONE) {
                index++;
                if(index == parsers.size()) {
                    result = composeResult();
                    status = Status.DONE;
                    return;
                }
                parser = parsers.get(index);
            }
            parser.parse(data);
        }
    }

    protected abstract T composeResult();
}
