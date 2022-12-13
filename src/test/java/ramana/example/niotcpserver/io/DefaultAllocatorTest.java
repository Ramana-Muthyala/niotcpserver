package ramana.example.niotcpserver.io;

import org.junit.jupiter.api.Test;
import ramana.example.niotcpserver.log.LogFactory;
import ramana.example.niotcpserver.types.LinkedList;
import ramana.example.niotcpserver.util.Constants;
import ramana.example.niotcpserver.util.TestUtil;

import java.lang.ref.ReferenceQueue;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Queue;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultAllocatorTest {
    private static final Logger logger = LogFactory.getLogger();
    @Test
    void testAllocation() throws NoSuchFieldException, IllegalAccessException {
        DefaultAllocator allocator = new DefaultAllocator();
        DefaultAllocator.Cache[] caches = (DefaultAllocator.Cache[]) TestUtil.invoke(allocator, "caches");

        Allocator.Resource<ByteBuffer> resource = allocator.allocate(Constants.READ_BUFFER_CAPACITY);
        assertFalse(resource.isReleased());
        resource.release();
        assertTrue(resource.isReleased());

        Queue smallCacheQueue = (Queue) TestUtil.invoke(caches[0], "queue");
        assertEquals(1, smallCacheQueue.size());
        resource = allocator.allocate(Constants.READ_BUFFER_CAPACITY);
        assertEquals(0, smallCacheQueue.size());
        resource.release();
        assertEquals(1, smallCacheQueue.size());

        Queue normalCacheQueue = (Queue) TestUtil.invoke(caches[1], "queue");
        assertEquals(0, normalCacheQueue.size());
        resource = allocator.allocate(Constants.CACHE_NORMAL_CAPACITY);
        assertEquals(0, normalCacheQueue.size());
        resource.release();
        assertEquals(1, normalCacheQueue.size());

        resource = allocator.allocate(Constants.CACHE_NORMAL_CAPACITY << 1);
        assertEquals(allocator.getClass().getName() + "$UnCachedResource", resource.getClass().getName());
        assertFalse(resource.isReleased());
        resource.release();
        assertTrue(resource.isReleased());

        assertEquals(1, smallCacheQueue.size());
        assertEquals(1, normalCacheQueue.size());
    }

    @Test
    void testAllocatorGCOne() throws NoSuchFieldException, IllegalAccessException {
        DefaultAllocator allocator = new DefaultAllocator();
        LinkedList referenceList = (LinkedList) TestUtil.invoke(allocator, "referenceList");
        ReferenceQueue referenceQueue = (ReferenceQueue) TestUtil.invoke(allocator, "referenceQueue");
        DefaultAllocator.Cache[] caches = (DefaultAllocator.Cache[]) TestUtil.invoke(allocator, "caches");
        Queue smallCacheQueue = (Queue) TestUtil.invoke(caches[0], "queue");
        Queue normalCacheQueue = (Queue) TestUtil.invoke(caches[1], "queue");

        assertEquals(0, smallCacheQueue.size());
        assertEquals(0, normalCacheQueue.size());
        assertEquals(caches[1], allocator.sizes.cacheFor(caches, Constants.READ_BUFFER_CAPACITY + 10));
        ArrayList<Allocator.Resource<ByteBuffer>> list = new ArrayList<>();
        for (int i = 0; i < Constants.CACHE_NORMAL_SIZE + 2; i++) {
            Allocator.Resource<ByteBuffer> resource = allocator.allocate(Constants.READ_BUFFER_CAPACITY + 10);
            list.add(resource);

        }
        Allocator.Resource<ByteBuffer> unCachedResource = allocator.allocate(Constants.CACHE_NORMAL_CAPACITY + 10);
        assertFalse(unCachedResource.isReleased());
        unCachedResource.release();
        assertTrue(unCachedResource.isReleased());
        assertEquals(0, smallCacheQueue.size());
        assertEquals(0, normalCacheQueue.size());
        assertEquals(Constants.CACHE_NORMAL_SIZE + 2, TestUtil.size(referenceList));
        forceGC();
        assertEquals(0, smallCacheQueue.size());
        assertEquals(0, normalCacheQueue.size());
        assertEquals(Constants.CACHE_NORMAL_SIZE + 2, TestUtil.size(referenceList));
        list.clear();
        forceGC();
        allocator.recycle();
        assertEquals(0, smallCacheQueue.size());
        assertEquals(Constants.CACHE_NORMAL_SIZE, normalCacheQueue.size());
        assertEquals(Constants.CACHE_NORMAL_SIZE, TestUtil.size(referenceList));
        assertNull(referenceQueue.poll());
    }

    @Test
    void testAllocatorGCTwo() throws NoSuchFieldException, IllegalAccessException {
        DefaultAllocator allocator = new DefaultAllocator();
        LinkedList referenceList = (LinkedList) TestUtil.invoke(allocator, "referenceList");
        ReferenceQueue referenceQueue = (ReferenceQueue) TestUtil.invoke(allocator, "referenceQueue");
        DefaultAllocator.Cache[] caches = (DefaultAllocator.Cache[]) TestUtil.invoke(allocator, "caches");
        Queue smallCacheQueue = (Queue) TestUtil.invoke(caches[0], "queue");
        Queue normalCacheQueue = (Queue) TestUtil.invoke(caches[1], "queue");

        assertEquals(0, smallCacheQueue.size());
        for (int i = 0; i < Constants.CACHE_SMALL_SIZE + 2; i++) {
            Allocator.Resource<ByteBuffer> resource = allocator.allocate(Constants.READ_BUFFER_CAPACITY);
            assertEquals(0, smallCacheQueue.size());
            resource.release();
            assertEquals(1, smallCacheQueue.size());
        }
        assertEquals(1, smallCacheQueue.size());
        assertEquals(0, normalCacheQueue.size());
        assertEquals(1, TestUtil.size(referenceList));
        forceGC();
        allocator.recycle();
        assertEquals(1, smallCacheQueue.size());
        assertEquals(0, normalCacheQueue.size());
        assertEquals(1, TestUtil.size(referenceList));
        assertNull(referenceQueue.poll());  // only one resource is in cache and that is not still enqueued.
    }

    @Test
    void testAllocatorGCThree() throws NoSuchFieldException, IllegalAccessException {
        DefaultAllocator allocator = new DefaultAllocator();
        LinkedList referenceList = (LinkedList) TestUtil.invoke(allocator, "referenceList");
        ReferenceQueue referenceQueue = (ReferenceQueue) TestUtil.invoke(allocator, "referenceQueue");
        DefaultAllocator.Cache[] caches = (DefaultAllocator.Cache[]) TestUtil.invoke(allocator, "caches");
        Queue smallCacheQueue = (Queue) TestUtil.invoke(caches[0], "queue");
        Queue normalCacheQueue = (Queue) TestUtil.invoke(caches[1], "queue");

        assertEquals(0, smallCacheQueue.size());
        assertEquals(0, normalCacheQueue.size());
        assertEquals(caches[1], allocator.sizes.cacheFor(caches, Constants.READ_BUFFER_CAPACITY + 10));
        ArrayList<Allocator.Resource<ByteBuffer>> list = new ArrayList<>();
        for (int i = 0; i < Constants.CACHE_NORMAL_SIZE + 2; i++) {
            Allocator.Resource<ByteBuffer> resource = allocator.allocate(Constants.READ_BUFFER_CAPACITY + 10);
            list.add(resource);

        }
        Allocator.Resource<ByteBuffer> unCachedResource = allocator.allocate(Constants.CACHE_NORMAL_CAPACITY + 10);
        assertFalse(unCachedResource.isReleased());
        unCachedResource.release();
        assertTrue(unCachedResource.isReleased());
        assertEquals(0, smallCacheQueue.size());
        assertEquals(0, normalCacheQueue.size());
        assertEquals(Constants.CACHE_NORMAL_SIZE + 2, TestUtil.size(referenceList));
        forceGC();
        assertEquals(0, smallCacheQueue.size());
        assertEquals(0, normalCacheQueue.size());
        assertEquals(Constants.CACHE_NORMAL_SIZE + 2, TestUtil.size(referenceList));
        for (Allocator.Resource<ByteBuffer> resource: list) {
            resource.release();
        }
        assertEquals(Constants.CACHE_NORMAL_SIZE, TestUtil.size(referenceList));
        list.clear();
        forceGC();
        allocator.recycle();
        assertEquals(0, smallCacheQueue.size());
        assertEquals(Constants.CACHE_NORMAL_SIZE, normalCacheQueue.size());
        assertEquals(Constants.CACHE_NORMAL_SIZE, TestUtil.size(referenceList));
        assertNull(referenceQueue.poll());
    }

    private void forceGC() {
        try {
            StringBuilder builder = new StringBuilder();
            while(true) builder.append("Memory drainer.......");
        } catch (OutOfMemoryError error) {
            logger.info(error.getClass().getName() + ": " + error.getMessage());
        }
    }
}
