package ramana.example.niotcpserver.codec.parser;

import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.List;

public class EitherOf<T, P extends AbstractParser<T>> extends AbstractPushbackParser<T> {
    private final List<P> parsers;
    int index;

    protected EitherOf(List<P> parsers, Deque<ByteBuffer> dataDeque) {
        super(dataDeque);
        this.parsers = parsers;
    }


    @Override
    public void parse(ByteBuffer data) throws ParseException {

    }
}
