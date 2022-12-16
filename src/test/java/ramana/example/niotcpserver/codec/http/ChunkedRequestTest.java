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
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class ChunkedRequestTest {
    private static final Logger logger = LogFactory.getLogger();

    @Test
    void testOne() throws InternalException {
        DefaultAllocator allocator = new DefaultAllocator();
        RequestCodec requestCodec = new RequestCodec();
        ParseException exception = null;
        try {
            testChunkedBodyRequestException(allocator, requestCodec);
        } catch (ParseException e) {
            logger.log(Level.INFO, "Expected ParseException.", e);
            exception = e;
        }
        assertNotNull(exception);
    }

    private void testChunkedBodyRequestException(DefaultAllocator allocator, RequestCodec requestCodec) throws ParseException, InternalException {
        String body = "4\r\n" +
                "Wiki\r\n" +
                "6\r\n" +
                "pedia \r\n" +
                "E\r\n" +
                "in \r\n" + "\r\n" + "chunks.\r\n" +
                "0\r\n" +
                "\r\n";
        String request = "POST /index.html?name=ramana HTTP/1.1\r\n" +
                "Host:localhost\r\n" +
                "User-Agent:NIOTCPServer/1.0\r\n" +
                "Accept:*.*\r\n" +
                "Accept-Encoding:gzip, deflate, br\r\n" +
                "Connection:keep-alive\r\n" +
                "Content-Length:" + body.getBytes().length + "\r\n" +
                "Transfer-Encoding:chunked\r\n" +
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
    }

    @Test
    void testTwo() throws InternalException, ParseException {
        DefaultAllocator allocator = new DefaultAllocator();
        RequestCodec requestCodec = new RequestCodec();
        testChunkedBodyRequest(allocator, requestCodec);
    }

    private void testChunkedBodyRequest(DefaultAllocator allocator, RequestCodec requestCodec) throws ParseException, InternalException {
        String body = "4\r\n" +
                "Wiki\r\n" +
                "6\r\n" +
                "pedia \r\n" +
                "E\r\n" +
                "in \r\n" + "\r\n" + "chunks.\r\n" +
                "0\r\n" +
                "\r\n";
        String request = "POST /index.html?name=ramana HTTP/1.1\r\n" +
                "Host:localhost\r\n" +
                "User-Agent:NIOTCPServer/1.0\r\n" +
                "Accept:*.*\r\n" +
                "Accept-Encoding:gzip, deflate, br\r\n" +
                "Connection:keep-alive\r\n" +
                "Transfer-Encoding:chunked\r\n" +
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

        byte[] actualBody = requestMessage.body;
        assertEquals(24, actualBody.length);
        assertTrue(new String(actualBody).endsWith("."));
        TestUtil.print(requestMessage);
    }

    @Test
    void testThree() throws InternalException, ParseException {
        DefaultAllocator allocator = new DefaultAllocator();
        RequestCodec requestCodec = new RequestCodec();
        testChunkedBodyRequest(allocator, requestCodec);
        testChunkedBodyWithChunkExtensionsRequest(allocator, requestCodec);
        testChunkedBodyWithChunkExtensionsTrailersRequest(allocator, requestCodec);
    }

    private void testChunkedBodyWithChunkExtensionsRequest(DefaultAllocator allocator, RequestCodec requestCodec) throws ParseException, InternalException {
        String body = "4;file=abc.txt;quality=0.7\r\n" +
                "Wiki\r\n" +
                "6\r\n" +
                "pedia \r\n" +
                "E ; some other=some thing else ;  quality=0.7\r\n" +
                "in \r\n" + "\r\n" + "chunks.\r\n" +
                "0\r\n" +
                "\r\n";
        String request = "POST /index.html?name=ramana HTTP/1.1\r\n" +
                "Host:localhost\r\n" +
                "User-Agent:NIOTCPServer/1.0\r\n" +
                "Accept:*.*\r\n" +
                "Accept-Encoding:gzip, deflate, br\r\n" +
                "Connection:keep-alive\r\n" +
                "Transfer-Encoding:chunked\r\n" +
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

        byte[] actualBody = requestMessage.body;
        assertEquals(24, actualBody.length);
        assertTrue(new String(actualBody).endsWith("."));
        TestUtil.print(requestMessage);
    }

    private void testChunkedBodyWithChunkExtensionsTrailersRequest(DefaultAllocator allocator, RequestCodec requestCodec) throws InternalException, ParseException {
        String body = "4;file=abc.txt;quality=0.7\r\n" +
                "Wiki\r\n" +
                "6\r\n" +
                "pedia \r\n" +
                "E ; some other=some thing else ;  quality=0.7\r\n" +
                "in \r\n" + "\r\n" + "chunks.\r\n" +
                "0\r\n" +
                "User-Agent:NIOTCPServer(Should not overwrite)/1.0\r\n" +
                "Accept:*.*(Should not overwrite)\r\n" +
                "Accept-Encoding:gzip, deflate, br(Should not overwrite)\r\n" +
                "Trailer1:Test trailer/1.0\r\n" +
                "Trailer2:Test trailer/1.0\r\n" +
                "\r\n";
        String request = "POST /index.html?name=ramana HTTP/1.1\r\n" +
                "Host:localhost\r\n" +
                "User-Agent:NIOTCPServer/1.0\r\n" +
                "Accept:*.*\r\n" +
                "Accept-Encoding:gzip, deflate, br\r\n" +
                "Connection:keep-alive\r\n" +
                "Transfer-Encoding:chunked\r\n" +
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
        assertEquals(8, requestMessage.headers.size());

        byte[] actualBody = requestMessage.body;
        assertEquals(24, actualBody.length);
        assertTrue(new String(actualBody).endsWith("."));
        TestUtil.print(requestMessage);
    }
}
