package ramana.example.niotcpserver.codec.parser;

import java.nio.ByteBuffer;

public class OneByte extends AbstractParser<Byte> {
    private final byte expected;

    public OneByte(byte expected) {
        this.expected = expected;
    }

    @Override
    public void parse(ByteBuffer data) throws ParseException {
        if(status == Status.DONE) return;
        status = Status.IN_PROGRESS;
        if(!data.hasRemaining()) return;
        byte tmp = data.get();
        if(tmp == expected) {
            result = tmp;
            status = Status.DONE;
            return;
        }
        throw new ParseException("Expected: " + expected + ", Found: " + tmp);
    }
}
