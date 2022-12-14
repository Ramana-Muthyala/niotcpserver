package ramana.example.niotcpserver.codec.parser;

public class DelimiterStringParser extends DelimiterParser<String> {
    public DelimiterStringParser(byte delimiter, byte breakPoint, int maxParseLength, int accumulatorCapacity) {
        super(delimiter, breakPoint, maxParseLength, accumulatorCapacity);
    }

    @Override
    protected void composeResult() {
        result = accumulator.getAsString();
    }
}
