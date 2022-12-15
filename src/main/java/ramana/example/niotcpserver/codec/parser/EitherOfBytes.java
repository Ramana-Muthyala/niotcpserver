package ramana.example.niotcpserver.codec.parser;

import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;

public class EitherOfBytes extends AbstractParser<Byte> {
    private final byte[] bytesToCompare;

    public EitherOfBytes(byte[] bytesToCompare) {
        this.bytesToCompare = bytesToCompare;
    }

    @Override
    public void parse(Allocator.Resource<ByteBuffer> data) throws ParseException, InternalException {
        if(status == Status.DONE) return;
        status = Status.IN_PROGRESS;
        ByteBuffer byteBuffer = data.get();
        if(!byteBuffer.hasRemaining()) return;
        byte tmp = byteBuffer.get();
        for (byte aByte: bytesToCompare) {
            if(tmp == aByte) {
                result = tmp;
                status = Status.DONE;
                return;
            }
        }
        if(result == null) throw new ParseException("Expected byte not matched. Found: " + tmp);
    }
}
