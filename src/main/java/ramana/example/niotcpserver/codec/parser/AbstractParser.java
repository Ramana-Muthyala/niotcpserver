package ramana.example.niotcpserver.codec.parser;

import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;

public abstract class AbstractParser<T> {
    protected Status status = Status.NONE;
    protected T result;

    public abstract void parse(Allocator.Resource<ByteBuffer> data) throws ParseException, InternalException;

    public Status getStatus() {
        return status;
    }

    public T getResult() {
        return result;
    }

    protected void reset() {
        result = null;
        status = Status.NONE;
    }

    public enum Status {NONE, IN_PROGRESS, DONE}
}
