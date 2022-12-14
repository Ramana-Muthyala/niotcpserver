package ramana.example.niotcpserver.codec.parser;

import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;

public class OneByte extends AbstractParser<Byte> {
    private final byte expected;

    public OneByte(byte expected) {
        this.expected = expected;
    }

    @Override
    public void parse(Allocator.Resource<ByteBuffer> data) throws ParseException, InternalException {
        if(status == Status.DONE) return;
        status = Status.IN_PROGRESS;
        ByteBuffer byteBuffer = data.get();
        if(!byteBuffer.hasRemaining()) return;
        byte tmp = byteBuffer.get();
        if(tmp == expected) {
            result = tmp;
            status = Status.DONE;
            return;
        }
        throw new ParseException("Expected: " + expected + ", Found: " + tmp);
    }
}
