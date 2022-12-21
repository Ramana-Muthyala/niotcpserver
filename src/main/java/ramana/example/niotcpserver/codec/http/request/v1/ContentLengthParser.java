package ramana.example.niotcpserver.codec.http.request.v1;

import ramana.example.niotcpserver.codec.parser.Accumulator;
import ramana.example.niotcpserver.codec.parser.ParseException;
import ramana.example.niotcpserver.codec.parser.v1.AbstractStateParser;
import ramana.example.niotcpserver.codec.parser.v1.Buffer;
import ramana.example.niotcpserver.types.InternalException;

public class ContentLengthParser extends AbstractStateParser {
    private final RequestMessage requestMessage;
    private final int length;
    private final Accumulator accumulator;
    private int index;

    public ContentLengthParser(RequestMessage requestMessage, Integer length) {
        this.requestMessage = requestMessage;
        this.length = length;
        accumulator = new Accumulator(length);
    }

    @Override
    public void parse(Buffer buffer) throws ParseException, InternalException {
        while(buffer.hasRemaining()) {
            byte tmp = buffer.read();
            accumulator.put(tmp);
            index++;
            if(index == length) {
                requestMessage.body = accumulator.getInternalByteArray();
                done = true;
                next = null;
                return;
            }
        }
    }
}
