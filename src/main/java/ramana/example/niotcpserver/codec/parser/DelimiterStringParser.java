package ramana.example.niotcpserver.codec.parser;

public class DelimiterStringParser extends DelimiterParser<String> {
    public DelimiterStringParser(byte delimiter, byte breakPoint, boolean rewind, int maxParseLength, int accumulatorCapacity) {
        super(delimiter, breakPoint, rewind, maxParseLength, accumulatorCapacity);
    }

    @Override
    protected void composeResult() {
        result = accumulator.getAsString();
    }
}
