package ramana.example.niotcpserver.codec.http;

import org.junit.jupiter.api.Test;
import ramana.example.niotcpserver.codec.http.request.RequestCodec;
import ramana.example.niotcpserver.codec.http.request.RequestMessage;
import ramana.example.niotcpserver.codec.parser.ParseException;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.io.DefaultAllocator;
import ramana.example.niotcpserver.log.LogFactory;
import ramana.example.niotcpserver.types.InternalException;
import ramana.example.niotcpserver.util.TestUtil;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class RequestCodecTest {
    private static final Logger logger = LogFactory.getLogger();
    @Test
    void testOne() throws InternalException, ParseException {
        DefaultAllocator allocator = new DefaultAllocator();
        RequestCodec requestCodec = new RequestCodec();
        testSimpleRequest(allocator, requestCodec);
    }

    private void testSimpleRequest(DefaultAllocator allocator, RequestCodec requestCodec) throws ParseException, InternalException {
        String request = "TRACE / HTTP/1.1\r\n" +
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
        assertEquals("TRACE", requestMessage.method);
        assertEquals("", requestMessage.path);
        assertEquals(0, requestMessage.queryParameters.size());
        assertEquals(0, requestMessage.headers.size());
        TestUtil.print(requestMessage);
    }

    @Test
    void testTwo() throws InternalException, ParseException {
        DefaultAllocator allocator = new DefaultAllocator();
        RequestCodec requestCodec = new RequestCodec();
        testHeadersRequest(allocator, requestCodec);
    }

    private void testHeadersRequest(DefaultAllocator allocator, RequestCodec requestCodec) throws ParseException, InternalException {
        String request = "GET /index.html?name=ramana HTTP/1.1\r\n" +
                "Host:localhost\r\n" +
                "User-Agent:NIOTCPServer/1.0\r\n" +
                "Accept:*.*\r\n" +
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
        assertEquals(RequestMessage.class, requestMessage.getClass());
        assertEquals("GET", requestMessage.method);
        assertEquals("index.html", requestMessage.path);
        assertEquals(1, requestMessage.queryParameters.size());
        assertEquals(5, requestMessage.headers.size());
        TestUtil.print(requestMessage);
    }

    @Test
    void testThree() throws InternalException, ParseException {
        DefaultAllocator allocator = new DefaultAllocator();
        RequestCodec requestCodec = new RequestCodec();
        testBodyRequest(allocator, requestCodec);
    }

    private void testBodyRequest(DefaultAllocator allocator, RequestCodec requestCodec) throws ParseException, InternalException {
        String body = "Hey, This is a POST message !!";
        String request = "POST /index.html?name=ramana HTTP/1.1\r\n" +
                "Host:localhost\r\n" +
                "User-Agent:NIOTCPServer/1.0\r\n" +
                "Accept:*.*\r\n" +
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
        assertEquals(RequestMessage.class, requestMessage.getClass());
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

    @Test
    void testFour() throws InternalException, ParseException {
        DefaultAllocator allocator = new DefaultAllocator();
        RequestCodec requestCodec = new RequestCodec();
        testSimpleRequest(allocator, requestCodec);
        testHeadersRequest(allocator, requestCodec);
        testBodyRequest(allocator, requestCodec);
    }

    @Test
    void testFive() throws InternalException, ParseException {
        DefaultAllocator allocator = new DefaultAllocator();
        RequestCodec requestCodec = new RequestCodec();
        testSimpleRequest(allocator, requestCodec);
        ParseException exception = null;
        try {
            testInvalidRequest(allocator, requestCodec);
        } catch (ParseException e) {
            exception = e;
            logger.info(exception.getMessage());
            requestCodec = new RequestCodec();
        }
        assertNotNull(exception);
        testHeadersRequest(allocator, requestCodec);
    }

    private void testInvalidRequest(DefaultAllocator allocator, RequestCodec requestCodec) throws ParseException, InternalException {
        String body = "Hey, This is a POST message !!";
        String request = "POST /index.html?name=ramana HTTP/1.1\r\n" +
                "Host:localhost\r\n" +
                "User-Agent:NIOTCPServer/1.0\r\n" +
                "Accept:*.*\r\n" +
                "Accept-Encoding:gzip, deflate, br\r\n" +
                "Connection:keep-alive\r\n" +
                "Content-Length:" + body.getBytes().length + "\r\n" +
                "\r\n" +
                body + "Extra padding to trigger invalid request";

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
}
