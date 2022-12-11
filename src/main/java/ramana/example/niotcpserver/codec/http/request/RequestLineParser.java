package ramana.example.niotcpserver.codec.http.request;

import ramana.example.niotcpserver.codec.parser.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Deque;

public class RequestLineParser extends CompositeParser<RequestLine> {
    public RequestLineParser(Deque<ByteBuffer> dataDeque) {
        super(dataDeque);
        parsers.add(new EitherOf(createMethodParsers(), dataDeque));
        parsers.add(Util.createSpaceParser());
        parsers.add(new RequestTargetParser());
        parsers.add(Util.createSpaceParser());
        parsers.add(new ByteSequence("HTTP/".getBytes()));
        parsers.add(Util.createDigitParser());
        parsers.add(Util.createDotParser());
        parsers.add(Util.createDigitParser());
        parsers.add(Util.createCRLFParser());
    }

    private ArrayList<ByteSequence> createMethodParsers() {
        ArrayList<ByteSequence> methodParsers = new ArrayList<>();
        methodParsers.add(new ByteSequence("GET".getBytes()));
        methodParsers.add(new ByteSequence("HEAD".getBytes()));
        methodParsers.add(new ByteSequence("POST".getBytes()));
        methodParsers.add(new ByteSequence("PUT".getBytes()));
        methodParsers.add(new ByteSequence("DELETE".getBytes()));
        methodParsers.add(new ByteSequence("CONNECT".getBytes()));
        methodParsers.add(new ByteSequence("OPTIONS".getBytes()));
        methodParsers.add(new ByteSequence("TRACE".getBytes()));
        return methodParsers;
    }

    @Override
    protected RequestLine composeResult() {
        String method = new String((byte[])parsers.get(0).getResult());
        RequestTarget requestTarget = (RequestTarget) parsers.get(2).getResult();
        return new RequestLine(method, requestTarget.path, requestTarget.queryString);
    }
}
