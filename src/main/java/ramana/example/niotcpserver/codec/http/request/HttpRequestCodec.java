package ramana.example.niotcpserver.codec.http.request;

import ramana.example.niotcpserver.codec.parser.AbstractParser;
import ramana.example.niotcpserver.codec.parser.ParseException;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;

public class HttpRequestCodec {
    private final AbstractParser<HttpRequestMessage> parser;
    public HttpRequestCodec() {
        AbstractParser firstParser = null;
        parser = new HttpRequestParser(firstParser);
    }

    public void decode(Allocator.Resource<ByteBuffer> data) throws ParseException, InternalException {
        ByteBuffer byteBuffer = data.get();
        byteBuffer.flip();
        parser.parse(byteBuffer);
    }

    public HttpRequestMessage get() {
        return parser.getResult();
    }

    public boolean isDecoded() {
        return parser.getStatus() == AbstractParser.Status.DONE;
    }
}
