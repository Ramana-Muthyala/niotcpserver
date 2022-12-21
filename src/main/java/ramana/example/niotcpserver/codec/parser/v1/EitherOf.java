package ramana.example.niotcpserver.codec.parser.v1;

import ramana.example.niotcpserver.types.InternalException;

import java.util.ArrayList;

public class EitherOf<T, P extends ByteParser<T>> implements ByteParser<T> {
    private final ArrayList<P> parsers;
    private int index;
    private T result;

    public EitherOf(ArrayList<P> parsers) {
        this.parsers = parsers;
    }

    @Override
    public Status parse(Buffer buffer) throws InternalException {
        if(index == 0) buffer.mark();

        do {
            P parser = parsers.get(index);
            Status status = parser.parse(buffer);
            if(status == Status.DONE) {
                buffer.release();
                result = parser.getResult();
                return Status.DONE;
            }
            if(status == Status.FAIL) {
                buffer.reset();
                index++;
            } else {
                return Status.IN_PROGRESS;
            }
        } while(index < parsers.size());

        return Status.FAIL;
    }

    @Override
    public T getResult() {
        return result;
    }
}
