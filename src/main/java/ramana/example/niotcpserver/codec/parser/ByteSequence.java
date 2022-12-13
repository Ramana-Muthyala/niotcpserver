package ramana.example.niotcpserver.codec.parser;

import java.nio.ByteBuffer;

public class ByteSequence extends AbstractParser<byte[]> {
    private final byte[] byteSequence;
    int index;

    public ByteSequence(byte[] byteSequence) {
        this.byteSequence = byteSequence;
    }

    @Override
    public void parse(ByteBuffer data) throws ParseException {
        if(status == Status.DONE) return;
        status = Status.IN_PROGRESS;
        while(data.hasRemaining()  &&  index < byteSequence.length) {
            byte tmp = data.get();
            if(tmp != byteSequence[index]) throw new ParseException("Expected: " + byteSequence[index] + ", Found: " + tmp);
            index++;
        }
        if(index == byteSequence.length) {
            status = Status.DONE;
            result = byteSequence;
        }
    }

    @Override
    protected void reset() {
        index = 0;
        super.reset();
    }
}
