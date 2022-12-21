package ramana.example.niotcpserver.codec.parser.v1;

import ramana.example.niotcpserver.types.InternalException;

public class SkipParser implements ByteParser<Byte> {
    private final byte delimiter;
    private final byte breakPoint;
    private final int maxLength;
    private int index;
    private Byte result;

    public SkipParser(byte delimiter, byte breakPoint, int maxLength) {
        this.delimiter = delimiter;
        this.breakPoint = breakPoint;
        this.maxLength = maxLength;
    }

    @Override
    public Status parse(Buffer buffer) throws InternalException {
        while(buffer.hasRemaining()) {
            byte tmp = buffer.read();
            if(tmp == delimiter  ||  tmp == breakPoint) {
                result = tmp;
                return Status.DONE;
            }
            index++;
            if(index == maxLength) return Status.FAIL;
        }
        return Status.IN_PROGRESS;
    }

    @Override
    public Byte getResult() {
        return result;
    }
}
