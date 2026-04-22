package ramana.example.niotcpserver.codec.parser;

public class MultipleDelimiterStringParser extends MultipleDelimiterParser<String> {
    public MultipleDelimiterStringParser(byte[] delimiters, byte breakPoint, boolean rewind, int maxParseLength, int accumulatorCapacity) {
        super(delimiters, breakPoint, rewind, maxParseLength, accumulatorCapacity);
    }

    @Override
    protected void composeResult() {
        result = accumulator.getAsString();
    }
}
