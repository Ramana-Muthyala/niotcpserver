package ramana.example.niotcpserver.codec.parser;

import java.nio.ByteBuffer;

public class DelimiterParser extends AbstractParser<String> {
    private final byte delimiter;
    private final Accumulator accumulator;
    private final int maxParseLength;
    private int index;

    public DelimiterParser(byte delimiter, int maxParseLength, int accumulatorCapacity) {
        this.delimiter = delimiter;
        this.maxParseLength = maxParseLength;
        this.accumulator = new Accumulator(accumulatorCapacity);
    }
    @Override
    public void parse(ByteBuffer data) throws ParseException {
        status = Status.IN_PROGRESS;
        while(data.hasRemaining()) {
            if(index == maxParseLength) throw new ParseException();
            byte tmp = data.get();
            if(tmp == delimiter) {
                data.position(data.position() - 1);
                result = accumulator.get();
                status = Status.DONE;
                return;
            }
            accumulator.put(tmp);
            index++;
        }
    }
}
