package ramana.example.niotcpserver.codec.http.v1;

import org.junit.jupiter.api.Test;
import ramana.example.niotcpserver.codec.http.request.v1.RequestCodec;
import ramana.example.niotcpserver.codec.http.request.v1.RequestMessage;
import ramana.example.niotcpserver.codec.parser.ParseException;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.io.DefaultAllocator;
import ramana.example.niotcpserver.log.LogFactory;
import ramana.example.niotcpserver.types.InternalException;
import ramana.example.niotcpserver.util.TestUtil;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class RequestCodecTest {
    private static final Logger logger = LogFactory.getLogger();

    @Test
    void testOne() throws InternalException, ParseException {
        DefaultAllocator allocator = new DefaultAllocator(ByteBuffer::allocate);
        RequestCodec requestCodec = new RequestCodec();
        testSimpleRequest(allocator, requestCodec);
    }

    @Test
    void testTwo() throws InternalException, ParseException {
        DefaultAllocator allocator = new DefaultAllocator(ByteBuffer::allocate);
        RequestCodec requestCodec = new RequestCodec();
        testAsteriskFormRequest(allocator, requestCodec);
    }

    @Test
    void testThree() throws InternalException {
        DefaultAllocator allocator = new DefaultAllocator(ByteBuffer::allocate);
        RequestCodec requestCodec = new RequestCodec();
        testAuthorityFormRequest(allocator, requestCodec);
    }

    @Test
    void testFour() throws InternalException, ParseException {
        DefaultAllocator allocator = new DefaultAllocator(ByteBuffer::allocate);
        RequestCodec requestCodec = new RequestCodec();
        testAbsoluteFormRequest(allocator, requestCodec);
    }

    @Test
    void testFive() throws InternalException, ParseException {
        DefaultAllocator allocator = new DefaultAllocator(ByteBuffer::allocate);
        RequestCodec requestCodec = new RequestCodec();
        testSimpleRequest(allocator, requestCodec);
        testAsteriskFormRequest(allocator, requestCodec);
        testAuthorityFormRequest(allocator, requestCodec);
        requestCodec = new RequestCodec();
        testAbsoluteFormRequest(allocator, requestCodec);
    }

    @Test
    void testHeadersOne() throws InternalException, ParseException {
        DefaultAllocator allocator = new DefaultAllocator(ByteBuffer::allocate);
        RequestCodec requestCodec = new RequestCodec();

        String request = "GET /index.html?name=ramana HTTP/1.1\r\n" +
                "Host:localhost\r\n" +
                "User-Agent:NIOTCPServer/1.0\r\n" +
                "Accept:*/*\r\n" +
                "Accept-Encoding:gzip, deflate, br\r\n" +
                "Connection:keep-alive\r\n" +
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

        RequestMessage requestMessage = requestCodec.get()[0];
        assertEquals("GET", requestMessage.method);
        assertEquals("index.html", requestMessage.path);
        assertEquals(1, requestMessage.queryParameters.size());
        assertEquals(5, requestMessage.headers.size());
        TestUtil.print(requestMessage);
    }

    @Test
    void testBody() throws InternalException, ParseException {
        DefaultAllocator allocator = new DefaultAllocator(ByteBuffer::allocate);
        RequestCodec requestCodec = new RequestCodec();

        String body = "Hey, This is a POST message !!";
        String request = "POST /index.html?name=ramana HTTP/1.1\r\n" +
                "Host:localhost\r\n" +
                "User-Agent:NIOTCPServer/1.0\r\n" +
                "Accept:*/*\r\n" +
                "Accept-Encoding:gzip, deflate, br\r\n" +
                "Connection:keep-alive\r\n" +
                "Content-Length:" + body.getBytes().length + "\r\n" +
                "\r\n" +
                body;
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

        RequestMessage requestMessage = requestCodec.get()[0];
        assertEquals("POST", requestMessage.method);
        assertEquals("index.html", requestMessage.path);
        assertEquals(1, requestMessage.queryParameters.size());
        assertEquals(6, requestMessage.headers.size());
        assertEquals(body.getBytes().length, TestUtil.getContentLength(requestMessage.headers));

        byte[] expectedBody = body.getBytes();
        byte[] actualBody = requestMessage.body;
        assertEquals(expectedBody.length, actualBody.length);
        for (int i = 0; i < expectedBody.length; i++) {
            assertEquals(expectedBody[i], actualBody[i]);
        }
        TestUtil.print(requestMessage);
    }

    private void testAbsoluteFormRequest(DefaultAllocator allocator, RequestCodec requestCodec) throws ParseException, InternalException {
        String request = "GET http://localhost:8080/tmp/index.html?n1=v1&n2=v2 HTTP/1.1\r\n" +
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

        RequestMessage requestMessage = requestCodec.get()[0];
        assertEquals(RequestMessage.class, requestMessage.getClass());
        assertEquals("GET", requestMessage.method);
        assertEquals("tmp/index.html", requestMessage.path);
        assertEquals(2, requestMessage.queryParameters.size());
        assertEquals(0, requestMessage.headers.size());
        assertNull(requestMessage.body);
        TestUtil.print(requestMessage);
    }

    private void testAuthorityFormRequest(DefaultAllocator allocator, RequestCodec requestCodec) throws InternalException {
        String request = "CONNECT localhost:8080 HTTP/1.1\r\n" +
                "\r\n";
        byte[] input = request.getBytes();
        int index = 0;
        ParseException exception = null;
        try {
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
        } catch (ParseException e) {
            logger.log(Level.INFO, "Expected exception: ", e);
            exception = e;
        }
        assertNotNull(exception);
    }

    private void testAsteriskFormRequest(DefaultAllocator allocator, RequestCodec requestCodec) throws InternalException, ParseException {
        String request = "OPTIONS * HTTP/1.1\r\n" +
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

        RequestMessage requestMessage = requestCodec.get()[0];
        assertEquals(RequestMessage.class, requestMessage.getClass());
        assertEquals("OPTIONS", requestMessage.method);
        assertEquals("*", requestMessage.path);
        assertNull(requestMessage.queryParameters);
        assertEquals(0, requestMessage.headers.size());
        assertNull(requestMessage.body);
        TestUtil.print(requestMessage);
    }

    private void testSimpleRequest(DefaultAllocator allocator, RequestCodec requestCodec) throws InternalException, ParseException {
        String request = "GET /index.html?n1=v1&n2=v2 HTTP/1.1\r\n" +
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

        RequestMessage requestMessage = requestCodec.get()[0];
        assertEquals(RequestMessage.class, requestMessage.getClass());
        assertEquals("GET", requestMessage.method);
        assertEquals("index.html", requestMessage.path);
        assertEquals(2, requestMessage.queryParameters.size());
        assertEquals(0, requestMessage.headers.size());
        assertNull(requestMessage.body);
        TestUtil.print(requestMessage);
    }
}
