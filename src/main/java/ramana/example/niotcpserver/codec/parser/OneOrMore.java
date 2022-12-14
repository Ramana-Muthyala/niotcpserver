package ramana.example.niotcpserver.codec.parser;

import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

public class OneOrMore<T, P extends AbstractParser<T>> extends AbstractParser<List<T>> {
    private final AbstractParser[] parsers = new AbstractParser[2];
    private int index;

    public OneOrMore(P parser, Deque<Allocator.Resource<ByteBuffer>> dataDeque) {
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
    public void parse(Allocator.Resource<ByteBuffer> data) throws ParseException, InternalException {
        if(status == Status.DONE) return;
        status = Status.IN_PROGRESS;
        ByteBuffer byteBuffer = data.get();
        while(byteBuffer.hasRemaining()) {
            AbstractParser parser = parsers[index];
            try {
                parser.parse(data);
            } catch (ParseCompleteSignalException exception) {
                composeResult(parser);
                status = Status.DONE;
                return;
            }
            if(parser.status == Status.DONE) {
                composeResult(parser);
                index++;
                if(index == parsers.length) {
                    status = Status.DONE;
                    return;
                }
            }
        }
    }

    @Override
    protected void reset() {
        index = 0;
        parsers[0].reset();
        parsers[1].reset();
        result = new ArrayList<>();
        status = Status.NONE;
    }
}
