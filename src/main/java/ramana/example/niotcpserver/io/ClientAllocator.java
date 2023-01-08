package ramana.example.niotcpserver.io;

import ramana.example.niotcpserver.util.Constants;

import java.nio.ByteBuffer;
import java.util.function.Function;

public class ClientAllocator extends DefaultAllocator {
    {
        sizes = new Sizes();
    }

    public ClientAllocator(Function<Integer, ByteBuffer> allocatorFunction) {
        super(allocatorFunction);
    }

    private class Sizes implements ISizes {
        private static final int smallCapacity = Constants.READ_BUFFER_CAPACITY;
        private static final int normalCapacity = Constants.CACHE_NORMAL_CAPACITY;
        private static final int smallSize = Constants.CACHE_CLIENT_SMALL_SIZE;
        private static final int normalSize = Constants.CACHE_CLIENT_NORMAL_SIZE;

        @Override
        public DefaultAllocator.Cache cacheFor(DefaultAllocator.Cache[] caches, int capacity) {
            if(capacity <= smallCapacity) return caches[0];
            if(capacity <= normalCapacity) return caches[1];
            return null;
        }

        @Override
        public DefaultAllocator.Cache[] buildCaches(DefaultAllocator allocator) {
            DefaultAllocator.Cache[] caches = new DefaultAllocator.Cache[2];
            caches[0] = new Cache(allocator, smallSize, smallCapacity);
            caches[1] = new Cache(allocator, normalSize, normalCapacity);
            return caches;
        }
    }
}
