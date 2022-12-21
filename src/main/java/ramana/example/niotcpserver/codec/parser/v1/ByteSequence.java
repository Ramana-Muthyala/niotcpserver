package ramana.example.niotcpserver.codec.parser.v1;

import ramana.example.niotcpserver.types.InternalException;

public class ByteSequence implements ByteParser<byte[]> {
    private final byte[] byteSequence;
    private int index;

    public ByteSequence(byte[] byteSequence) {
        this.byteSequence = byteSequence;
    }


    @Override
    public Status parse(Buffer buffer) throws InternalException {
        while(index < byteSequence.length  &&  buffer.hasRemaining()) {
            if(buffer.read() != byteSequence[index]) {
                return Status.FAIL;
            }
            index++;
        }
        if(index == byteSequence.length) return Status.DONE;
        return Status.IN_PROGRESS;
    }

    @Override
    public byte[] getResult() {
        return byteSequence;
    }
}
