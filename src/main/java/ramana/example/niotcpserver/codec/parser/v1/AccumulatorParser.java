package ramana.example.niotcpserver.codec.parser.v1;

import ramana.example.niotcpserver.codec.parser.Accumulator;
import ramana.example.niotcpserver.types.InternalException;

public abstract class AccumulatorParser<T> implements ByteParser<T> {
    private final byte delimiter;
    private final byte breakPoint;
    private final int maxSize;
    protected Accumulator accumulator;
    private int index;
    private byte lastRead;

    public AccumulatorParser(byte delimiter, byte breakPoint, int maxSize, int accumulatorCapacity) {
        this.delimiter = delimiter;
        this.breakPoint = breakPoint;
        this.maxSize = maxSize;
        this.accumulator = new Accumulator(accumulatorCapacity);
    }

    public byte getLastRead() {
        return lastRead;
    }

    @Override
    public Status parse(Buffer buffer) throws InternalException {
        while(buffer.hasRemaining()) {
            byte tmp = buffer.read();
            if(tmp == delimiter  ||  tmp == breakPoint) {
                lastRead = tmp;
                return Status.DONE;
            }
            accumulator.put(tmp);
            index++;
            if(index == maxSize) return Status.FAIL;
        }
        return Status.IN_PROGRESS;
    }

    @Override
    public void reset() {
        index = 0;
        accumulator.reset();
    }
}
