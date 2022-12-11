package ramana.example.niotcpserver.codec.http.response;

import ramana.example.niotcpserver.io.Allocator;

import java.nio.ByteBuffer;

public class ResponseCodec {
    public static ResponseCodec getInstance() {
        return null;
    }

    public Allocator.Resource<ByteBuffer> badRequest(Allocator<ByteBuffer> allocator) {
        return null;
    }

    public Allocator.Resource<ByteBuffer> encode(Allocator<ByteBuffer> allocator, ResponseMessage responseMessage) {
        return null;
    }
}
