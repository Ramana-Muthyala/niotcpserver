package ramana.example.niotcpserver.codec.http.v1;

import org.junit.jupiter.api.Test;
import ramana.example.niotcpserver.codec.parser.v1.Buffer;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.io.DefaultAllocator;
import ramana.example.niotcpserver.log.LogFactory;
import ramana.example.niotcpserver.types.InternalException;
import ramana.example.niotcpserver.util.TestUtil;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class BufferTest {
    private static final Logger logger = LogFactory.getLogger();

    @Test
    void testRead() throws InternalException, NoSuchFieldException, IllegalAccessException {
        DefaultAllocator allocator = new DefaultAllocator(ByteBuffer::allocate);
        Buffer buffer = new Buffer();
        for (int i = 0; i < 5; i++) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Allocator.Resource<ByteBuffer> resource = allocator.allocate(1024);
            ByteBuffer byteBuffer = resource.get();
            String message = "Hello !! This is a test message.";
            byteBuffer.put(message.getBytes());
            byteBuffer.flip();
            buffer.write(resource);
            while(buffer.hasRemaining()) {
                out.write(buffer.read());
            }
            assertTrue(resource.isReleased());
            String readString = out.toString();
            assertEquals(message, readString);
            assertEquals(0, ((ArrayList)TestUtil.invoke(buffer, "data")).size());
            assertEquals(0, ((int)TestUtil.invoke(buffer, "dataIndex")));
            assertEquals(0, ((int)TestUtil.invoke(buffer, "byteIndex")));
            assertFalse(((boolean)TestUtil.invoke(buffer, "mark")));
        }
    }

    @Test
    void testMark() throws InternalException, NoSuchFieldException, IllegalAccessException {
        DefaultAllocator allocator = new DefaultAllocator(ByteBuffer::allocate);
        Buffer buffer = new Buffer();
        mark0(allocator, buffer);
    }

    private void mark0(DefaultAllocator allocator, Buffer buffer) throws InternalException, NoSuchFieldException, IllegalAccessException {
        Allocator.Resource<ByteBuffer> resource = allocator.allocate(1024);
        ByteBuffer byteBuffer = resource.get();
        String firstMessage = "Hello !! This is a test message.";
        byteBuffer.put(firstMessage.getBytes());
        byteBuffer.flip();
        buffer.write(resource);
        consume(buffer, 5);
        assertEquals(0, ((ArrayList)TestUtil.invoke(buffer, "data")).size());
        assertEquals(0, ((int)TestUtil.invoke(buffer, "dataIndex")));
        assertEquals(0, ((int)TestUtil.invoke(buffer, "byteIndex")));
        assertFalse(((boolean)TestUtil.invoke(buffer, "mark")));
        buffer.mark();
        assertTrue(resource.isReleased());
        assertNull(TestUtil.invoke(buffer, "resource"));
        assertEquals(1, ((ArrayList)TestUtil.invoke(buffer, "data")).size());
        assertEquals(0, ((int)TestUtil.invoke(buffer, "dataIndex")));
        assertEquals(0, ((int)TestUtil.invoke(buffer, "byteIndex")));
        assertTrue(((boolean)TestUtil.invoke(buffer, "mark")));
        consume(buffer, 5);
        assertTrue(resource.isReleased());
        assertEquals(1, ((ArrayList)TestUtil.invoke(buffer, "data")).size());
        assertEquals(0, ((int)TestUtil.invoke(buffer, "dataIndex")));
        assertEquals(5, ((int)TestUtil.invoke(buffer, "byteIndex")));
        assertTrue(((boolean)TestUtil.invoke(buffer, "mark")));
        assertNull(TestUtil.invoke(buffer, "resource"));
        consume(buffer);
        assertTrue(resource.isReleased());
        assertEquals(1, ((ArrayList)TestUtil.invoke(buffer, "data")).size());
        assertEquals(1, ((int)TestUtil.invoke(buffer, "dataIndex")));
        assertEquals(0, ((int)TestUtil.invoke(buffer, "byteIndex")));
        assertTrue(((boolean)TestUtil.invoke(buffer, "mark")));
        assertNull(TestUtil.invoke(buffer, "resource"));
        assertTrue(resource.isReleased());

        resource = allocator.allocate(1024);
        byteBuffer = resource.get();
        String secondMessage = "Hello !! This is a test message. Let me make it a little longer.";
        byteBuffer.put(secondMessage.getBytes());
        byteBuffer.flip();
        buffer.write(resource);
        assertTrue(resource.isReleased());
        assertEquals(2, ((ArrayList)TestUtil.invoke(buffer, "data")).size());
        assertEquals(1, ((int)TestUtil.invoke(buffer, "dataIndex")));
        assertEquals(0, ((int)TestUtil.invoke(buffer, "byteIndex")));
        assertTrue(((boolean)TestUtil.invoke(buffer, "mark")));
        assertTrue(resource.isReleased());
        assertNull(TestUtil.invoke(buffer, "resource"));
        consume(buffer, 5);
        assertTrue(resource.isReleased());
        assertEquals(2, ((ArrayList)TestUtil.invoke(buffer, "data")).size());
        assertEquals(1, ((int)TestUtil.invoke(buffer, "dataIndex")));
        assertEquals(5, ((int)TestUtil.invoke(buffer, "byteIndex")));
        assertTrue(((boolean)TestUtil.invoke(buffer, "mark")));
        assertNull(TestUtil.invoke(buffer, "resource"));
        consume(buffer);
        assertTrue(resource.isReleased());
        assertEquals(2, ((ArrayList)TestUtil.invoke(buffer, "data")).size());
        assertEquals(2, ((int)TestUtil.invoke(buffer, "dataIndex")));
        assertEquals(0, ((int)TestUtil.invoke(buffer, "byteIndex")));
        assertTrue(((boolean)TestUtil.invoke(buffer, "mark")));
        assertNull(TestUtil.invoke(buffer, "resource"));
        assertTrue(resource.isReleased());
    }

    @Test
    void testMarkReset() throws InternalException, NoSuchFieldException, IllegalAccessException {
        DefaultAllocator allocator = new DefaultAllocator(ByteBuffer::allocate);
        Buffer buffer = new Buffer();

        mark0(allocator, buffer);
        reset0(allocator, buffer);
    }

    @Test
    void testMarkResetRelease() throws NoSuchFieldException, InternalException, IllegalAccessException {
        DefaultAllocator allocator = new DefaultAllocator(ByteBuffer::allocate);
        Buffer buffer = new Buffer();

        mark0(allocator, buffer);
        reset0(allocator, buffer);

        buffer.release();
        assertEquals(0, ((ArrayList)TestUtil.invoke(buffer, "data")).size());
        assertEquals(0, ((int)TestUtil.invoke(buffer, "dataIndex")));
        assertEquals(0, ((int)TestUtil.invoke(buffer, "byteIndex")));
        assertFalse(((boolean)TestUtil.invoke(buffer, "mark")));
        assertNull(TestUtil.invoke(buffer, "resource"));

        Allocator.Resource<ByteBuffer> resource = allocator.allocate(1024);
        ByteBuffer byteBuffer = resource.get();
        String fourthMessage = "Hello !! This is a test message. I am gonna add some data.";
        byteBuffer.put(fourthMessage.getBytes());
        byteBuffer.flip();
        buffer.write(resource);
        assertEquals(0, ((ArrayList)TestUtil.invoke(buffer, "data")).size());
        assertEquals(0, ((int)TestUtil.invoke(buffer, "dataIndex")));
        assertEquals(0, ((int)TestUtil.invoke(buffer, "byteIndex")));
        assertFalse(((boolean)TestUtil.invoke(buffer, "mark")));
        assertFalse(resource.isReleased());
        assertNotNull(TestUtil.invoke(buffer, "resource"));

        buffer.mark();
        consume(buffer, 4);
        assertEquals(1, ((ArrayList)TestUtil.invoke(buffer, "data")).size());
        assertEquals(0, ((int)TestUtil.invoke(buffer, "dataIndex")));
        assertEquals(4, ((int)TestUtil.invoke(buffer, "byteIndex")));
        assertTrue(((boolean)TestUtil.invoke(buffer, "mark")));
        assertTrue(resource.isReleased());
        assertNull(TestUtil.invoke(buffer, "resource"));

        resource = allocator.allocate(1024);
        byteBuffer = resource.get();
        String fifthMessage = "Hello !! This is a test message. I am gonna add some data....";
        byteBuffer.put(fourthMessage.getBytes());
        byteBuffer.flip();
        buffer.write(resource);
        assertEquals(2, ((ArrayList)TestUtil.invoke(buffer, "data")).size());
        assertEquals(0, ((int)TestUtil.invoke(buffer, "dataIndex")));
        assertEquals(4, ((int)TestUtil.invoke(buffer, "byteIndex")));
        assertTrue(((boolean)TestUtil.invoke(buffer, "mark")));
        assertTrue(resource.isReleased());
        assertNull(TestUtil.invoke(buffer, "resource"));

        buffer.release();
        assertEquals(2, ((ArrayList)TestUtil.invoke(buffer, "data")).size());
        assertEquals(0, ((int)TestUtil.invoke(buffer, "dataIndex")));
        assertEquals(4, ((int)TestUtil.invoke(buffer, "byteIndex")));
        assertFalse(((boolean)TestUtil.invoke(buffer, "mark")));
        assertTrue(resource.isReleased());
        assertNull(TestUtil.invoke(buffer, "resource"));

        consume(buffer, 3);
        assertEquals(2, ((ArrayList)TestUtil.invoke(buffer, "data")).size());
        assertEquals(0, ((int)TestUtil.invoke(buffer, "dataIndex")));
        assertEquals(7, ((int)TestUtil.invoke(buffer, "byteIndex")));
        assertFalse(((boolean)TestUtil.invoke(buffer, "mark")));
        assertTrue(resource.isReleased());
        assertNull(TestUtil.invoke(buffer, "resource"));

        consume(buffer);
        assertEquals(0, ((ArrayList)TestUtil.invoke(buffer, "data")).size());
        assertEquals(0, ((int)TestUtil.invoke(buffer, "dataIndex")));
        assertEquals(0, ((int)TestUtil.invoke(buffer, "byteIndex")));
        assertFalse(((boolean)TestUtil.invoke(buffer, "mark")));
        assertTrue(resource.isReleased());
        assertNull(TestUtil.invoke(buffer, "resource"));

        resource = allocator.allocate(1024);
        byteBuffer = resource.get();
        String sixthMessage = "Hello !! This is a test message....";
        byteBuffer.put(sixthMessage.getBytes());
        byteBuffer.flip();
        buffer.write(resource);
        assertEquals(0, ((ArrayList)TestUtil.invoke(buffer, "data")).size());
        assertEquals(0, ((int)TestUtil.invoke(buffer, "dataIndex")));
        assertEquals(0, ((int)TestUtil.invoke(buffer, "byteIndex")));
        assertFalse(((boolean)TestUtil.invoke(buffer, "mark")));
        assertFalse(resource.isReleased());
        assertNotNull(TestUtil.invoke(buffer, "resource"));

        consume(buffer, 5);
        assertEquals(0, ((ArrayList)TestUtil.invoke(buffer, "data")).size());
        assertEquals(0, ((int)TestUtil.invoke(buffer, "dataIndex")));
        assertEquals(0, ((int)TestUtil.invoke(buffer, "byteIndex")));
        assertFalse(((boolean)TestUtil.invoke(buffer, "mark")));
        assertNotNull(TestUtil.invoke(buffer, "resource"));
        assertTrue(resource.get().hasRemaining());
        assertFalse(resource.isReleased());

        consume(buffer);
        assertEquals(0, ((ArrayList)TestUtil.invoke(buffer, "data")).size());
        assertEquals(0, ((int)TestUtil.invoke(buffer, "dataIndex")));
        assertEquals(0, ((int)TestUtil.invoke(buffer, "byteIndex")));
        assertFalse(((boolean)TestUtil.invoke(buffer, "mark")));
        assertNull(TestUtil.invoke(buffer, "resource"));
        assertTrue(resource.isReleased());
    }

    private void reset0(DefaultAllocator allocator, Buffer buffer) throws InternalException, NoSuchFieldException, IllegalAccessException {
        buffer.reset();
        assertEquals(2, ((ArrayList)TestUtil.invoke(buffer, "data")).size());
        assertEquals(0, ((int)TestUtil.invoke(buffer, "dataIndex")));
        assertEquals(0, ((int)TestUtil.invoke(buffer, "byteIndex")));
        assertTrue(((boolean)TestUtil.invoke(buffer, "mark")));
        assertNull(TestUtil.invoke(buffer, "resource"));
        consume(buffer, 6);
        assertEquals(2, ((ArrayList)TestUtil.invoke(buffer, "data")).size());
        assertEquals(0, ((int)TestUtil.invoke(buffer, "dataIndex")));
        assertEquals(6, ((int)TestUtil.invoke(buffer, "byteIndex")));
        assertTrue(((boolean)TestUtil.invoke(buffer, "mark")));
        assertNull(TestUtil.invoke(buffer, "resource"));
        consume(buffer);
        assertEquals(2, ((ArrayList)TestUtil.invoke(buffer, "data")).size());
        assertEquals(2, ((int)TestUtil.invoke(buffer, "dataIndex")));
        assertEquals(0, ((int)TestUtil.invoke(buffer, "byteIndex")));
        assertTrue(((boolean)TestUtil.invoke(buffer, "mark")));
        assertNull(TestUtil.invoke(buffer, "resource"));
        buffer.reset();
        assertEquals(2, ((ArrayList)TestUtil.invoke(buffer, "data")).size());
        assertEquals(0, ((int)TestUtil.invoke(buffer, "dataIndex")));
        assertEquals(0, ((int)TestUtil.invoke(buffer, "byteIndex")));
        assertTrue(((boolean)TestUtil.invoke(buffer, "mark")));
        assertNull(TestUtil.invoke(buffer, "resource"));
        consume(buffer, 5);
        assertEquals(2, ((ArrayList)TestUtil.invoke(buffer, "data")).size());
        assertEquals(0, ((int)TestUtil.invoke(buffer, "dataIndex")));
        assertEquals(5, ((int)TestUtil.invoke(buffer, "byteIndex")));
        assertTrue(((boolean)TestUtil.invoke(buffer, "mark")));
        assertNull(TestUtil.invoke(buffer, "resource"));

        Allocator.Resource<ByteBuffer> resource = allocator.allocate(1024);
        ByteBuffer byteBuffer = resource.get();
        String thirdMessage = "Hello !! This is a test message. Modified....";
        byteBuffer.put(thirdMessage.getBytes());
        byteBuffer.flip();
        buffer.write(resource);
        assertEquals(3, ((ArrayList)TestUtil.invoke(buffer, "data")).size());
        assertEquals(0, ((int)TestUtil.invoke(buffer, "dataIndex")));
        assertEquals(5, ((int)TestUtil.invoke(buffer, "byteIndex")));
        assertTrue(((boolean)TestUtil.invoke(buffer, "mark")));
        assertTrue(resource.isReleased());
        assertNull(TestUtil.invoke(buffer, "resource"));

        consume(buffer, 5);
        assertEquals(3, ((ArrayList)TestUtil.invoke(buffer, "data")).size());
        assertEquals(0, ((int)TestUtil.invoke(buffer, "dataIndex")));
        assertEquals(10, ((int)TestUtil.invoke(buffer, "byteIndex")));
        assertTrue(((boolean)TestUtil.invoke(buffer, "mark")));
        assertTrue(resource.isReleased());
        assertNull(TestUtil.invoke(buffer, "resource"));

        buffer.reset();
        assertEquals(3, ((ArrayList)TestUtil.invoke(buffer, "data")).size());
        assertEquals(0, ((int)TestUtil.invoke(buffer, "dataIndex")));
        assertEquals(0, ((int)TestUtil.invoke(buffer, "byteIndex")));
        assertTrue(((boolean)TestUtil.invoke(buffer, "mark")));
        assertTrue(resource.isReleased());
        assertNull(TestUtil.invoke(buffer, "resource"));

        consume(buffer);
        assertEquals(3, ((ArrayList)TestUtil.invoke(buffer, "data")).size());
        assertEquals(3, ((int)TestUtil.invoke(buffer, "dataIndex")));
        assertEquals(0, ((int)TestUtil.invoke(buffer, "byteIndex")));
        assertTrue(((boolean)TestUtil.invoke(buffer, "mark")));
        assertTrue(resource.isReleased());
        assertNull(TestUtil.invoke(buffer, "resource"));
    }

    private void consume(Buffer buffer, int count) throws InternalException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = 0; i < count; i++) {
            if(buffer.hasRemaining()) out.write(buffer.read());
        }
        assertEquals(count, out.size());
    }

    private void consume(Buffer buffer) throws InternalException {
        while(buffer.hasRemaining()) buffer.read();
        assertFalse(buffer.hasRemaining());
    }
}
