package ramana.example.niotcpserver.codec.parser;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class ZeroOrMore<T, P extends AbstractParser<T>> extends AbstractPushbackParser<List<T>> {
    private final P parser;

    public ZeroOrMore(P parser, Deque<ByteBuffer> dataDeque) {
        super(dataDeque);
        this.parser = parser;
        result = new ArrayList<>();
    }

    @Override
    public void parse(ByteBuffer data) {
        if(status == Status.DONE) return;
        status = Status.IN_PROGRESS;
        data.mark();
        while(data.hasRemaining()) {
            try {
                parser.parse(data);
                if(parser.status == Status.DONE) {
                    stack.clear();
                    result.add(parser.result);
                    parser.reset();
                    data.mark();
                }
            } catch (ParseException exception) {
                status = Status.DONE;
                break;
            }
        }
        if(parser.status == Status.IN_PROGRESS) stack.offer(data);
        if(status == Status.DONE) pushBack();
    }
}
