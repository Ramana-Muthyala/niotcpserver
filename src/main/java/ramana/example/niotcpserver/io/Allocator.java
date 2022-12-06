package ramana.example.niotcpserver.io;

import ramana.example.niotcpserver.types.InternalException;

public interface Allocator<T> {
    Resource<T> allocate(int capacity);

    interface Resource<T> {
        T get() throws InternalException;
        void release();
        boolean isReleased();
    }
}
