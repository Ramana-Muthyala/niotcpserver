package ramana.example.niotcpserver.codec.http.request;

import ramana.example.niotcpserver.codec.parser.*;

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
    public void parse(ByteBuffer data) throws ParseException {
        if(status == Status.DONE) return;
        status = Status.IN_PROGRESS;
        while(data.hasRemaining()) {
            byte tmp = data.get();
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
