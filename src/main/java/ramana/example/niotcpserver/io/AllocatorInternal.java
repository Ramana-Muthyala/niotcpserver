package ramana.example.niotcpserver.io;

public interface AllocatorInternal<T> extends Allocator<T> {
    void recycle();
}
