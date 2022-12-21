package ramana.example.niotcpserver.codec.parser.v1;

public class StringAccumulatorParser extends AccumulatorParser<String> {
    public StringAccumulatorParser(byte[] delimiters, int maxSize, int accumulatorCapacity) {
        super(delimiters, maxSize, accumulatorCapacity);
    }

    @Override
    public String getResult() {
        return accumulator.getAsString();
    }
}
