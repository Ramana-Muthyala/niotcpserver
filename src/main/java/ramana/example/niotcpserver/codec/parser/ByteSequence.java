package ramana.example.niotcpserver.codec.parser;

import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;

public class ByteSequence extends AbstractParser<byte[]> {
    private final byte[] byteSequence;
    int index;

    public ByteSequence(byte[] byteSequence) {
        this.byteSequence = byteSequence;
    }

    @Override
    public void parse(Allocator.Resource<ByteBuffer> data) throws ParseException, InternalException {
        if(status == Status.DONE) return;
        status = Status.IN_PROGRESS;
        ByteBuffer byteBuffer = data.get();
        while(byteBuffer.hasRemaining()  &&  index < byteSequence.length) {
            byte tmp = byteBuffer.get();
            if(tmp != byteSequence[index]) throw new ParseException("Expected: " + byteSequence[index] + ", Found: " + tmp);
            index++;
        }
        if(index == byteSequence.length) {
            status = Status.DONE;
            result = byteSequence;
        }
    }

    @Override
    public void reset() {
        index = 0;
        super.reset();
    }
}
