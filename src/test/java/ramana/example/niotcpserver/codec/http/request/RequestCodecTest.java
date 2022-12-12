package ramana.example.niotcpserver.codec.http.request;

import org.junit.jupiter.api.Test;
import ramana.example.niotcpserver.codec.parser.ParseException;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.io.DefaultAllocator;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

public class RequestCodecTest {
    @Test
    void testOne() throws InternalException, ParseException {
        DefaultAllocator allocator = new DefaultAllocator();

        RequestCodec requestCodec = new RequestCodec();

        String request = "TRACE /index.html?name=ramana HTTP/1.1\r\n" +
                            "\r\n";
        byte[] input = request.getBytes();
        int index = 0;
        do {
            Allocator.Resource<ByteBuffer> resource = allocator.allocate(10);
            ByteBuffer byteBuffer = resource.get();
            for (int i = 0; i < 4; i++) {
                byteBuffer.put(input[index]);
                index++;
                if (index == input.length) break;
            }
            requestCodec.decode(resource);
        } while (index != input.length);

        assertTrue(requestCodec.isDecoded());
        RequestMessage requestMessage = requestCodec.get();
        assertEquals(RequestMessage.class, requestMessage.getClass());
        assertEquals("TRACE", requestMessage.method);
        assertEquals("index.html", requestMessage.path);
        assertEquals(1, requestMessage.queryParameters.size());
        assertEquals(0, requestMessage.headers.size());
    }
}
