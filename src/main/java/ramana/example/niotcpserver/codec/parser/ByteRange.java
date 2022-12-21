package ramana.example.niotcpserver.codec.parser;

import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;

public class ByteRange extends AbstractParser<Byte> {

    private final byte from;
    private final byte to;

    public ByteRange(byte from, byte to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public void parse(Allocator.Resource<ByteBuffer> data) throws ParseException, InternalException {
        if(status == Status.DONE) return;
        status = Status.IN_PROGRESS;
        ByteBuffer byteBuffer = data.get();
        if(!byteBuffer.hasRemaining()) return;
        byte tmp = byteBuffer.get();
        if(tmp < from  ||  tmp > to) throw new ParseException("Expected range: from: " + from + ", to: " + to + ". Found: " + tmp);
        status = Status.DONE;
    }
}
