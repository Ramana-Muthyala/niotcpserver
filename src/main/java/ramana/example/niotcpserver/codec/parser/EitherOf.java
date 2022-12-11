package ramana.example.niotcpserver.codec.parser;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Deque;

public class EitherOf<T, P extends AbstractParser<T>> extends AbstractPushbackParser<T> {
    private final ArrayList<P> parsers;
    int index;

    public EitherOf(ArrayList<P> parsers, Deque<ByteBuffer> dataDeque) {
        super(dataDeque);
        this.parsers = parsers;
    }


    @Override
    public void parse(ByteBuffer data) throws ParseException {
        status = Status.IN_PROGRESS;
        data.mark();
        while(data.hasRemaining()) {
            P parser = parsers.get(index);
            try {
                parser.parse(data);
                if(parser.status == Status.DONE) {
                    stack.clear();
                    result = parser.result;
                    status = Status.DONE;
                    return;
                }
            } catch (ParseException exception) {
                index++;
                pushBack();
                if(index == parsers.size()) throw exception;
            }
            if(parser.status == Status.IN_PROGRESS) stack.offer(data);
        }
    }
}
