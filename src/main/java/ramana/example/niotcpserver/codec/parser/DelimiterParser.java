package ramana.example.niotcpserver.codec.parser;

import java.nio.ByteBuffer;

public class DelimiterParser extends AbstractParser<String> {
    private final byte delimiter;
    private final Accumulator accumulator;
    private final int maxParseLength;
    private final byte breakPoint;
    private int index;

    public DelimiterParser(byte delimiter, byte breakPoint, int maxParseLength, int accumulatorCapacity) {
        this.delimiter = delimiter;
        this.breakPoint = breakPoint;
        this.maxParseLength = maxParseLength;
        this.accumulator = new Accumulator(accumulatorCapacity);
    }
    @Override
    public void parse(ByteBuffer data) throws ParseException {
        if(status == Status.DONE) return;
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
            if(tmp == breakPoint) throw new ParseException();
            accumulator.put(tmp);
            index++;
        }
    }
}
