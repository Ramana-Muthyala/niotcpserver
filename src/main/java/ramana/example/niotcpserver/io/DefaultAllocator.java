package ramana.example.niotcpserver.io;

import ramana.example.niotcpserver.types.InternalException;
import ramana.example.niotcpserver.types.LinkedList;
import ramana.example.niotcpserver.util.Constants;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

public class DefaultAllocator implements AllocatorInternal<ByteBuffer> {
    protected ISizes sizes = new Sizes();
    private final Cache[] caches = sizes.buildCaches(this);
    private final LinkedList<AllocatorReference> referenceList = new LinkedList<>();
    private final ReferenceQueue<CachedResource> referenceQueue = new ReferenceQueue<>();

    protected interface ISizes {
        DefaultAllocator.Cache cacheFor(DefaultAllocator.Cache[] caches, int capacity);
        DefaultAllocator.Cache[] buildCaches(DefaultAllocator allocator);
    }
    private static class Sizes implements ISizes {
        private static final int smallCapacity = Constants.READ_BUFFER_CAPACITY;
        private static final int normalCapacity = Constants.CACHE_NORMAL_CAPACITY;
        private static final int smallSize = Constants.CACHE_SMALL_SIZE;
        private static final int normalSize = Constants.CACHE_NORMAL_SIZE;

        @Override
        public DefaultAllocator.Cache cacheFor(DefaultAllocator.Cache[] caches, int capacity) {
            if(capacity <= smallCapacity) return caches[0];
            if(capacity <= normalCapacity) return caches[1];
            return null;
        }

        @Override
        public DefaultAllocator.Cache[] buildCaches(DefaultAllocator allocator) {
            DefaultAllocator.Cache[] caches = new DefaultAllocator.Cache[2];
            caches[0] = new DefaultAllocator.Cache(allocator, smallSize, smallCapacity);
            caches[1] = new DefaultAllocator.Cache(allocator, normalSize, normalCapacity);
            return caches;
        }
    }

    @Override
    public Allocator.Resource<ByteBuffer> allocate(int capacity) {
        Cache cache = sizes.cacheFor(caches, capacity);
        if(cache == null) return new UnCachedResource(capacity);
        return cache.allocate();
    }

    @Override
    public void recycle() {
        AllocatorReference reference;
        while((reference = (AllocatorReference) referenceQueue.poll()) != null) {
            referenceList.remove(reference.referenceNode);
            Cache cache = reference.cache;
            // reference.byteBuffer == null comparison is just an extra check
            if(cache.size == cache.queue.size()  ||  reference.byteBuffer == null) continue;
            reference.byteBuffer.clear();
            cache.queue.offer(new CachedResource(reference.byteBuffer, cache));
        }
    }

    private static class AllocatorReference extends WeakReference<CachedResource> {
        private ByteBuffer byteBuffer;
        private LinkedList.LinkedNode<AllocatorReference> referenceNode;
        private final Cache cache;

        private AllocatorReference(CachedResource referent) {
            super(referent, referent.cache.allocator.referenceQueue);
            this.byteBuffer = referent.byteBuffer;
            this.cache = referent.cache;
        }
    }

    protected static class Cache {
        private final int capacity;
        private final Queue<CachedResource> queue;
        private final DefaultAllocator allocator;
        private final int size;

        protected Cache(DefaultAllocator allocator, int size, int capacity) {
            this.allocator = allocator;
            this.size = size;
            this.capacity = capacity;
            queue = new ArrayDeque<>(size);
        }

        private CachedResource allocate() {
            CachedResource resource = queue.poll();
            if(resource != null) return resource;
            return new CachedResource(this);
        }
    }

    private static class UnCachedResource implements Allocator.Resource<ByteBuffer> {
        protected final ByteBuffer byteBuffer;
        protected boolean released;

        private UnCachedResource(int capacity) {
            byteBuffer = ByteBuffer.allocateDirect(capacity);
        }

        private UnCachedResource(ByteBuffer byteBuffer) {
            this.byteBuffer = byteBuffer;
        }

        @Override
        public ByteBuffer get() throws InternalException {
            if(released) throw new InternalException(InternalException.RESOURCE_RELEASED);
            return byteBuffer;
        }

        @Override
        public void release() {
            released = true;
        }

        @Override
        public boolean isReleased() {
            return released;
        }
    }

    private static class CachedResource extends UnCachedResource {
        private final Cache cache;
        private final LinkedList.LinkedNode<AllocatorReference> referenceNode;

        private CachedResource(Cache cache) {
            super(cache.capacity);
            this.cache = cache;
            AllocatorReference reference = new AllocatorReference(this);
            referenceNode = cache.allocator.referenceList.add(reference);
            reference.referenceNode = referenceNode;
        }

        private CachedResource(ByteBuffer byteBuffer, Cache cache) {
            super(byteBuffer);
            this.cache = cache;
            AllocatorReference reference = new AllocatorReference(this);
            referenceNode = cache.allocator.referenceList.add(reference);
            reference.referenceNode = referenceNode;
        }

        @Override
        public void release() {
            if(released) return;
            released = true;
            cache.allocator.referenceList.remove(referenceNode);
            if(cache.size == cache.queue.size()) return;
            referenceNode.value.byteBuffer = null;  // This is just an extra check
            byteBuffer.clear();
            cache.queue.offer(new CachedResource(byteBuffer, cache));
        }
    }
}