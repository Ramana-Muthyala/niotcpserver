package ramana.example.niotcpserver.codec.parser;

import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;

public abstract class DelimiterParser<T> extends AbstractParser<T> {
    private final byte delimiter;
    protected Accumulator accumulator;
    private final int maxParseLength;
    private final int accumulatorCapacity;
    private final byte breakPoint;
    private int index;

    /*
    * DelimiterParser is not suitable for repeatable (such as ZeroOrMore, OneOrMore) parsing as it
    * rewinds the byte read when delimiter or breakpoint is reached.
    * */
    public DelimiterParser(byte delimiter, byte breakPoint, int maxParseLength, int accumulatorCapacity) {
        this.delimiter = delimiter;
        this.breakPoint = breakPoint;
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
            if(tmp == delimiter) {
                byteBuffer.position(byteBuffer.position() - 1);
                composeResult();
                status = Status.DONE;
                return;
            }
            if(tmp == breakPoint) {
                byteBuffer.position(byteBuffer.position() - 1);
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
