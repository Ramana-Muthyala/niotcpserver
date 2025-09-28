package ramana.example.niotcpserver.util;

public interface Constants {
    int SELECTION_KEY_OP_NONE = 0;
    int MIN_BACKLOG = 1;
    int MIN_IDLE_TIMEOUT = 0;
    int DEFAULT_NUM_WORKERS = Runtime.getRuntime().availableProcessors() * 2;
    long SERVER_SHUTDOWN_TIMEOUT = 20000; // milliseconds
    long CHANNEL_CLOSE_TIMEOUT = 5000; // milliseconds
    long SSL_CHANNEL_CLOSE_TIMEOUT = 10000; // milliseconds

    int READ_BUFFER_CAPACITY = 1 << 12;
    int CACHE_NORMAL_CAPACITY = 1 << 16;
    int CACHE_SMALL_SIZE = 256;
    int CACHE_NORMAL_SIZE = 64;
    int CACHE_CLIENT_SMALL_SIZE = 16;
    int CACHE_CLIENT_NORMAL_SIZE = 16;
}
