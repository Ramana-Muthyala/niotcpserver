package ramana.example.niotcpserver.codec.http.response;

import ramana.example.niotcpserver.io.Allocator;

import java.nio.ByteBuffer;

public class HttpResponseCodec {
    public static HttpResponseCodec getInstance() {
        return null;
    }

    public Allocator.Resource<ByteBuffer> badRequest(Allocator<ByteBuffer> allocator) {
        return null;
    }

    public Allocator.Resource<ByteBuffer> encode(Allocator<ByteBuffer> allocator, HttpResponseMessage responseMessage) {
        return null;
    }
}
