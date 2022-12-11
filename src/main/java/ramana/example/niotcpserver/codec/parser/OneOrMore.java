package ramana.example.niotcpserver.codec.parser;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

public class OneOrMore<T, P extends AbstractParser<T>> extends AbstractParser<List<T>> {
    private final AbstractParser[] parsers = new AbstractParser[2];
    private int index;

    public OneOrMore(P parser, Deque<ByteBuffer> dataDeque) {
        parsers[0] = parser;
        parsers[1] = new ZeroOrMore<>(parser, dataDeque);
        result = new ArrayList<>();
    }

    private void composeResult(AbstractParser parser) {
        if(parser instanceof ZeroOrMore) {
            result.addAll((Collection<T>) parser.result);
        } else {
            result.add((T) parser.result);
            parser.reset();     // reset as the same parser instance is used in ZeroOrMore
        }
    }

    @Override
    public void parse(ByteBuffer data) throws ParseException {
        status = Status.IN_PROGRESS;
        while(data.hasRemaining()) {
            AbstractParser parser = parsers[index];
            if(parser.status == Status.DONE) {
                composeResult(parser);
                index++;
                if(index == parsers.length) {
                    status = Status.DONE;
                    return;
                }
                parser = parsers[index];
            }
            parser.parse(data);
        }
    }
}
