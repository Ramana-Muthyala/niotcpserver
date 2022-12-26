package ramana.example.niotcpserver.codec.http.request.v1;

import ramana.example.niotcpserver.codec.http.Util;
import ramana.example.niotcpserver.codec.parser.ParseException;
import ramana.example.niotcpserver.codec.parser.v1.*;
import ramana.example.niotcpserver.types.InternalException;

import java.util.ArrayList;

public class MethodParser extends AbstractStateParser {
    private final RequestMessage requestMessage;
    private final EitherOf<byte[], ByteSequence> parser;
    byte state;

    public MethodParser(RequestMessage requestMessage) {
        this.requestMessage = requestMessage;
        ArrayList<ByteSequence> parsers = new ArrayList<>(8);
        parsers.add(new ByteSequence(Util.GET));
        parsers.add(new ByteSequence(Util.HEAD));
        parsers.add(new ByteSequence(Util.POST));
        parsers.add(new ByteSequence(Util.PUT));
        parsers.add(new ByteSequence(Util.DELETE));
        parsers.add(new ByteSequence(Util.CONNECT));
        parsers.add(new ByteSequence(Util.OPTIONS));
        parsers.add(new ByteSequence(Util.TRACE));
        parsers.add(new ByteSequence(Util.PATCH));
        this.parser = new EitherOf<>(parsers);
    }

    @Override
    public void parse(Buffer buffer) throws ParseException, InternalException {
        if(state == 0) {
            ByteParser.Status status = parser.parse(buffer);
            if(status == ByteParser.Status.DONE) {
                requestMessage.method = new String(parser.getResult());
                state++;
            } else if (status == ByteParser.Status.FAIL) {
                throw new ParseException();
            }
        }
        if(state == 1) {
            if(!buffer.hasRemaining()) return;
            if(buffer.read() != Util.SP) throw new ParseException();
            done = true;
            next = new PathParser(requestMessage);
        }
    }
}
