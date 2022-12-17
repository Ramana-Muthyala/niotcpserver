package ramana.example.niotcpserver.codec.parser;

import ramana.example.niotcpserver.io.Allocator;

import java.nio.ByteBuffer;

public class ParseCompletePushBackSignalException extends ParseException {
    public final Allocator.Resource<ByteBuffer> pushBackPoint;

    public ParseCompletePushBackSignalException(ParseException exception, Allocator.Resource<ByteBuffer> pushBackPoint) {
        super(exception);
        this.pushBackPoint = pushBackPoint;
    }
}
