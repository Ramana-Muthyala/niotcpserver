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
        status = Status.IN_PROGRESS;
        while(data.hasRemaining()  &&  index < byteSequence.length) {
            if(data.get() != byteSequence[index]) throw new ParseException();
            index++;
        }
        if(index == byteSequence.length) {
            status = Status.DONE;
            result = byteSequence;
        }
    }
}
