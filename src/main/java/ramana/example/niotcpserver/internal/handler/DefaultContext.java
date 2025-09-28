package ramana.example.niotcpserver.internal.handler;

import ramana.example.niotcpserver.handler.ChannelHandler;
import ramana.example.niotcpserver.handler.Context;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.Event;
import ramana.example.niotcpserver.types.InternalException;
import ramana.example.niotcpserver.types.LinkedList;

import java.nio.ByteBuffer;

public class DefaultContext implements Context.OnConnect, Context.OnClose {
    private final InternalChannelHandler internalChannelHandler;
    LinkedList.LinkedNode<ChannelHandler> channelHandlerNode;
    boolean invalidated;
    Event event;
    Throwable cause;

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
    public Allocator<ByteBuffer> allocator() {
        return internalChannelHandler.allocator;
    }

    @Override
    public void close() throws InternalException {
        internalChannelHandler.close(this);
    }

    @Override
    public void next(Object data) throws InternalException {
        internalChannelHandler.next(this, data);
    }

    @Override
    public void next(Object data, Throwable cause) throws InternalException {
        internalChannelHandler.next(this, data, cause);
    }
}
