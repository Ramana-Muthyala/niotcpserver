package ramana.example.niotcpserver.internal.handler;

import ramana.example.niotcpserver.handler.ChannelHandler;
import ramana.example.niotcpserver.handler.Context;
import ramana.example.niotcpserver.internal.worker.AsyncPool;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.Event;
import ramana.example.niotcpserver.types.InternalException;
import ramana.example.niotcpserver.types.LinkedList;
import ramana.example.niotcpserver.types.TriConsumer;

import java.nio.ByteBuffer;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class DefaultContext implements Context.OnConnect, Context.OnClose {
    private final InternalChannelHandler internalChannelHandler;
    LinkedList.LinkedNode<ChannelHandler> channelHandlerNode;
    boolean invalidated;
    Event event;
    Throwable cause;
    private boolean asyncInvoked;

    public void resetAsyncInvoked() {
        asyncInvoked = false;
    }

    DefaultContext(InternalChannelHandler internalChannelHandler, LinkedList.LinkedNode<ChannelHandler> channelHandlerNode, Event event) {
        this.internalChannelHandler = internalChannelHandler;
        this.channelHandlerNode = channelHandlerNode;
        this.event = event;
    }

    DefaultContext(InternalChannelHandler internalChannelHandler, LinkedList.LinkedNode<ChannelHandler> channelHandlerNode, Throwable cause) {
        this.internalChannelHandler = internalChannelHandler;
        this.channelHandlerNode = channelHandlerNode;
        this.cause = cause;
    }

    public DefaultContext(InternalChannelHandler internalChannelHandler) {
        this.internalChannelHandler = internalChannelHandler;
    }

    public String getSocketAddress() {
        return internalChannelHandler.getSocketAddress();
    }

    @Override
    public boolean isDefaultReadEnabled() {
        return internalChannelHandler.defaultRead;
    }

    @Override
    public void fireReadInterest() throws InternalException {
        internalChannelHandler.fireReadInterest(this);
    }

    @Override
    public void clearReadInterest() throws InternalException {
        internalChannelHandler.clearReadInterest(this);
    }

    @Override
    public void write(Object data) throws InternalException {
        internalChannelHandler.write(this, data);
    }

    @Override
    public void flush() throws InternalException {
        internalChannelHandler.flush(this);
    }

    @Override
    public <T, R> void async(T input, Function<T, R> task, BiConsumer<R, Context.OnWrite> callback, TriConsumer<T, Context.OnWrite, Exception> errorCallback) throws InternalException {
        if(task == null  ||  callback == null  ||  errorCallback == null) {
            close();
            return;
        }
        asyncInvoked = true;
        AsyncPool.Task<T, R> asyncTask = new AsyncPool.Task<>(input, task, this, callback, errorCallback, internalChannelHandler.worker);
        internalChannelHandler.worker.asyncPool.execute(asyncTask);
    }

    @Override
    public Allocator<ByteBuffer> allocator() {
        return internalChannelHandler.allocator;
    }

    @Override
    public void close() throws InternalException {
        internalChannelHandler.close(this);
    }

    @Override
    public void next(Object data) throws InternalException {
        if(asyncInvoked) return; // NoOp if async() was invoked.
        internalChannelHandler.next(this, data);
    }

    @Override
    public void next(Object data, Throwable cause) throws InternalException {
        internalChannelHandler.next(this, data, cause);
    }
}
