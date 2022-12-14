package ramana.example.niotcpserver.codec.http.request;

import ramana.example.niotcpserver.codec.parser.*;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;

public class ContentLengthBasedBodyParser extends AbstractParser<byte[]> {
    private final Accumulator accumulator;
    private int index;
    private final int contentLength;

    public ContentLengthBasedBodyParser(int contentLength) {
        this.contentLength = contentLength;
        accumulator = new Accumulator(contentLength);
    }

    @Override
    public void parse(Allocator.Resource<ByteBuffer> data) throws ParseException, InternalException {
        if(status == Status.DONE) return;
        status = Status.IN_PROGRESS;
        ByteBuffer byteBuffer = data.get();
        while(byteBuffer.hasRemaining()) {
            byte tmp = byteBuffer.get();
            accumulator.put(tmp);
            index++;
            if(index == contentLength) {
                status = Status.DONE;
                result = accumulator.getInternalByteArray();
                return;
            }
        }
    }
}
