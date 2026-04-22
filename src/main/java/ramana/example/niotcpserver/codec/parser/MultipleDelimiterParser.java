package ramana.example.niotcpserver.codec.parser;

import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;

public abstract class MultipleDelimiterParser<T> extends AbstractParser<T> {
    private final byte[] delimiters;
    protected Accumulator accumulator;
    private final int maxParseLength;
    private final int accumulatorCapacity;
    private final byte breakPoint;
    private final boolean rewind;
    private int index;

    public MultipleDelimiterParser(byte[] delimiters, byte breakPoint, boolean rewind, int maxParseLength, int accumulatorCapacity) {
        this.delimiters = delimiters;
        this.breakPoint = breakPoint;
        this.rewind = rewind;
        this.maxParseLength = maxParseLength;
        this.accumulatorCapacity = accumulatorCapacity;
        this.accumulator = new Accumulator(accumulatorCapacity);
    }
    @Override
    public void parse(Allocator.Resource<ByteBuffer> data) throws ParseException, InternalException {
        if(status == Status.DONE) return;
        status = Status.IN_PROGRESS;
        ByteBuffer byteBuffer = data.get();
        while(byteBuffer.hasRemaining()) {
            if(index == maxParseLength) throw new ParseException();
            byte tmp = byteBuffer.get();
            for (byte delimiter: delimiters) {
                if(tmp == delimiter) {
                    if(rewind) byteBuffer.position(byteBuffer.position() - 1);
                    composeResult();
                    status = Status.DONE;
                    return;
                }
            }
            if(tmp == breakPoint) {
                if(rewind) byteBuffer.position(byteBuffer.position() - 1);
                composeResult();
                throw new DelimiterBreakPointParseException();
            }
            accumulator.put(tmp);
            index++;
        }
    }

    protected abstract void composeResult();

    @Override
    public void reset() {
        index = 0;
        this.accumulator = new Accumulator(accumulatorCapacity);
        super.reset();
    }
}
