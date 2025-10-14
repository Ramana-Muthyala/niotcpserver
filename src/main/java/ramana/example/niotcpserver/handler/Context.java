package ramana.example.niotcpserver.handler;

import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;
import ramana.example.niotcpserver.types.TriConsumer;

import java.nio.ByteBuffer;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface Context {
    interface OnConnect extends OnRead {
        boolean isDefaultReadEnabled();
        void fireReadInterest() throws InternalException;
    }

    interface OnRead extends OnWrite {
        void clearReadInterest() throws InternalException;
    }

    interface OnWrite {
        Allocator<ByteBuffer> allocator();
        void write(Object data) throws InternalException;
        void flush() throws InternalException;

        /*
        * next() call should not be invoked after async() is invoked.
        * if next() call is invoked after async() method invocation, it is treated as NoOp.
        * next() call can be invoked in callback methods so that channel handler chain can be triggered.
        * It is like proceeding where we left off and that is the correct way of doing it.
        *
        * Parameters:
        * input may be null.
        * If task or callback or errorCallback are null, InternalException is thrown.
         * */
        <T, R> void async(T input, Function<T, R> task, BiConsumer<R, OnWrite> callback, TriConsumer<T, OnWrite, Exception> errorCallback) throws InternalException;
        void close() throws InternalException;
        void next(Object data) throws InternalException;
    }

    interface OnClose {
        void next(Object data, Throwable cause) throws InternalException;
    }
}
