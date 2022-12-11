package ramana.example.niotcpserver.codec.parser;

import java.nio.ByteBuffer;

public class EitherOfBytes extends AbstractParser<Byte> {
    private final byte[] bytesToCompare;

    public EitherOfBytes(byte[] bytesToCompare) {
        this.bytesToCompare = bytesToCompare;
    }

    @Override
    public void parse(ByteBuffer data) throws ParseException {
        status = Status.IN_PROGRESS;
        if(!data.hasRemaining()) return;
        byte tmp = data.get();
        for (byte aByte: bytesToCompare) {
            if(tmp == aByte) {
                result = tmp;
                status = Status.DONE;
                break;
            }
        }
        if(result == null) throw new ParseException();
    }
}
