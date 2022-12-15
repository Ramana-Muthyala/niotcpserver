package ramana.example.niotcpserver.codec.parser;

import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;

public class FixedLengthParser extends AbstractParser<byte[]> {
    private Accumulator accumulator;
    private int index;
    private final int length;

    public FixedLengthParser(int length) {
        this.length = length;
        accumulator = new Accumulator(length);
    }

    @Override
    public void parse(Allocator.Resource<ByteBuffer> data) throws ParseException, InternalException {
        if(status == Status.DONE) return;
        status = Status.IN_PROGRESS;
        ByteBuffer byteBuffer = data.get();
        while(byteBuffer.hasRemaining()) {
            byte tmp = byteBuffer.get();
            accumulator.put(tmp);
            index++;
            if(index == length) {
                status = Status.DONE;
                result = accumulator.getInternalByteArray();
                return;
            }
        }
    }

    @Override
    public void reset() {
        index = 0;
        accumulator = new Accumulator(length);
        super.reset();
    }
}
