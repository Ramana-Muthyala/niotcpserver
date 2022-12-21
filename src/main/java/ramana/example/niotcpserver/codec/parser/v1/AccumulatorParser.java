package ramana.example.niotcpserver.codec.parser.v1;

import ramana.example.niotcpserver.codec.parser.Accumulator;
import ramana.example.niotcpserver.types.InternalException;

public abstract class AccumulatorParser<T> implements ByteParser<T> {
    private final byte[] delimiters;
    private final int maxSize;
    protected Accumulator accumulator;
    private int index;
    private byte lastRead;

    public AccumulatorParser(byte[] delimiters, int maxSize, int capacity) {
        this.delimiters = delimiters;
        this.maxSize = maxSize;
        this.accumulator = new Accumulator(capacity);
    }

    public byte getLastRead() {
        return lastRead;
    }

    @Override
    public Status parse(Buffer buffer) throws InternalException {
        while(buffer.hasRemaining()) {
            byte tmp = buffer.read();
            for (byte delimiter: delimiters) {
                if(tmp == delimiter) {
                    lastRead = tmp;
                    return Status.DONE;
                }
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
