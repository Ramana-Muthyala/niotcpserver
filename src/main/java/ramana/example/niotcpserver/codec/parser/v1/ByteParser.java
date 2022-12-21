package ramana.example.niotcpserver.codec.parser.v1;

import ramana.example.niotcpserver.types.InternalException;

public interface ByteParser<T> {
    Status parse(Buffer buffer) throws InternalException;
    T getResult();

    enum Status {IN_PROGRESS, FAIL, DONE}

    default void reset() {}
}
