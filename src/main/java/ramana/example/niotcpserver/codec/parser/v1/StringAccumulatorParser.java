package ramana.example.niotcpserver.codec.parser.v1;

public class StringAccumulatorParser extends AccumulatorParser<String> {
    public StringAccumulatorParser(byte delimiter, byte breakPoint, int maxSize, int accumulatorCapacity) {
        super(delimiter, breakPoint, maxSize, accumulatorCapacity);
    }

    @Override
    public String getResult() {
        return accumulator.getAsString();
    }
}
