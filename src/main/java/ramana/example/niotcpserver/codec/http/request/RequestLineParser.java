package ramana.example.niotcpserver.codec.http.request;

import ramana.example.niotcpserver.codec.http.Util;
import ramana.example.niotcpserver.codec.parser.ByteSequence;
import ramana.example.niotcpserver.codec.parser.CompositeParser;
import ramana.example.niotcpserver.codec.parser.EitherOf;

import java.util.ArrayList;

public class RequestLineParser extends CompositeParser<RequestLine> {
    public RequestLineParser() {
        parsers.add(new EitherOf<>(createMethodParsers()));
        parsers.add(Util.createSpaceParser());
        parsers.add(new RequestTargetParser());
        parsers.add(Util.createSpaceParser());
        parsers.add(new ByteSequence(Util.HTTP_SLASH));
        parsers.add(Util.createDigitParser());
        parsers.add(Util.createDotParser());
        parsers.add(Util.createDigitParser());
        parsers.add(Util.createCRLFParser());
    }

    private ArrayList<ByteSequence> createMethodParsers() {
        ArrayList<ByteSequence> methodParsers = new ArrayList<>();
        methodParsers.add(new ByteSequence(Util.GET));
        methodParsers.add(new ByteSequence(Util.HEAD));
        methodParsers.add(new ByteSequence(Util.POST));
        methodParsers.add(new ByteSequence(Util.PUT));
        methodParsers.add(new ByteSequence(Util.DELETE));
        methodParsers.add(new ByteSequence(Util.CONNECT));
        methodParsers.add(new ByteSequence(Util.OPTIONS));
        methodParsers.add(new ByteSequence(Util.TRACE));
        methodParsers.add(new ByteSequence(Util.PATCH));
        return methodParsers;
    }

    @Override
    protected RequestLine composeResult() {
        String method = new String((byte[])parsers.get(0).getResult());
        RequestTarget requestTarget = (RequestTarget) parsers.get(2).getResult();
        return new RequestLine(method, requestTarget.path, requestTarget.queryString);
    }
}
