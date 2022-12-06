package ramana.example.niotcpserver.handler;

import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;

public interface Context {
    interface OnConnect extends OnRead {
        void fireReadInterest() throws InternalException;
    }

    interface OnRead extends OnWrite {
        void clearReadInterest() throws InternalException;
    }

    interface OnWrite {
        Allocator<ByteBuffer> allocator();
        void write(Object data) throws InternalException;
        void flush() throws InternalException;
        void close() throws InternalException;
        void next(Object data) throws InternalException;
    }

    interface OnClose {
        void next(Object data, Throwable cause) throws InternalException;
    }
}
