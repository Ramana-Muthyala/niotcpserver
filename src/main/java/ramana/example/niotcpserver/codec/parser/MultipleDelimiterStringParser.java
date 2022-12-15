package ramana.example.niotcpserver.codec.parser;

public class MultipleDelimiterStringParser extends MultipleDelimiterParser<String> {
    public MultipleDelimiterStringParser(byte[] delimiters, byte breakPoint, int maxParseLength, int accumulatorCapacity) {
        super(delimiters, breakPoint, maxParseLength, accumulatorCapacity);
    }

    @Override
    protected void composeResult() {
        result = accumulator.getAsString();
    }
}
