package ramana.example.niotcpserver.worker.impl;

import ramana.example.niotcpserver.handler.ChannelHandler;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.io.ChannelOperations;
import ramana.example.niotcpserver.log.LogFactory;
import ramana.example.niotcpserver.types.ChannelHandlerMethodName;
import ramana.example.niotcpserver.types.InternalException;
import ramana.example.niotcpserver.types.LinkedList;

import java.io.IOError;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InternalChannelHandler {
    protected static final Logger logger = LogFactory.getLogger();
    protected final LinkedList<ChannelHandler> channelHandlers;
    protected final Allocator<ByteBuffer> allocator;
    protected final boolean defaultRead;
    protected ChannelOperations channelOperations;
    protected boolean closeInitiated;
    private final ContextFactory factory;
    private String socketAddress;

    public String getSocketAddress() {
        return socketAddress;
    }

    public InternalChannelHandler(LinkedList<ChannelHandler> channelHandlers, Allocator<ByteBuffer> allocator, ContextFactory factory, boolean defaultRead) {
        this.channelHandlers = channelHandlers;
        this.allocator = allocator;
        this.factory = factory;
        this.defaultRead = defaultRead;
    }

    public void handleWrite() {
        try {
            channelOperations.flush();
        } catch (OutOfMemoryError | IOError | IOException | RuntimeException | InternalException exception) {
            logger.log(Level.INFO, exception.getMessage(), exception);
            channelOperations.close(true);
        }
    }

    public void handleRead() {
        LinkedList.LinkedNode<ChannelHandler> channelHandlerNode = channelHandlers.head;
        DefaultContext context = factory.newContext(this, channelHandlerNode, ChannelHandlerMethodName.onRead);
        try {
            Object data = channelOperations.read();
            if(data instanceof Integer) {
                if((int) data == 0) {
                    // no data read
                    return;
                }
                context.channelHandlerMethodName = ChannelHandlerMethodName.onReadComplete;
                channelHandlerNode.value.onReadComplete(context, null);
                onClose(context);
            } else {
                channelHandlerNode.value.onRead(context, data);
            }
        } catch (OutOfMemoryError | IOError | IOException | RuntimeException | InternalException exception) {
            if(context.invalidated) return;
            context.cause = exception;
            onClose(context);
        }
    }

    public void onConnect(SelectionKey sk) {
        SocketChannel channel = (SocketChannel) sk.channel();
        channelOperations = new ChannelOperations(allocator, sk);
        LinkedList.LinkedNode<ChannelHandler> channelHandlerNode = channelHandlers.head;
        DefaultContext context = factory.newContext(this, channelHandlerNode, ChannelHandlerMethodName.onConnect);
        try {
            this.socketAddress = "[" + channel.getLocalAddress().toString() + " - " + channel.getRemoteAddress().toString() + "]";
            channelHandlerNode.value.onConnect(context, null);
        } catch (OutOfMemoryError | IOError | RuntimeException | InternalException | IOException exception) {
            if(context.invalidated) return;
            context.cause = exception;
            onClose(context);
        }
    }

    private void onClose(DefaultContext context) {
        if(closeInitiated) return;  // to prevent close being called multiple times in a sequence.
        closeInitiated = true;
        channelOperations.flushAndClose();
        DefaultContext newContext = factory.newContext(this, context.channelHandlerNode, context.cause);
        try {
            newContext.channelHandlerNode.value.onClose(newContext, null, newContext.cause);
        } catch (OutOfMemoryError | RuntimeException | InternalException exception) {
            if(newContext.invalidated) return;
            if(newContext.cause != null) exception.addSuppressed(newContext.cause);
            newContext.cause = exception;
            next(newContext, null, exception);
        }
    }

    public void next(DefaultContext context, Object data, Throwable cause) {
        if(context.invalidated) return;
        context.invalidated = true;
        if(context.cause != cause) {
            logger.info("Exception dispatched is not same as exception forwarded. Handler: " + context.channelHandlerNode.value.getClass().getName());
            logger.log(Level.INFO, "Exception dispatched: ", context.cause);
            logger.log(Level.INFO, "Exception forwarded: ", cause);
        }
        LinkedList.LinkedNode<ChannelHandler> prevChannelHandlerNode = context.channelHandlerNode.prev;
        if(prevChannelHandlerNode == null) return;
        DefaultContext newContext = factory.newContext(this, prevChannelHandlerNode, cause);
        try {
            prevChannelHandlerNode.value.onClose(newContext, data, newContext.cause);
        } catch (OutOfMemoryError | RuntimeException | InternalException exception) {
            if(newContext.invalidated) return;
            if(newContext.cause != null) exception.addSuppressed(newContext.cause);
            newContext.cause = exception;
            next(newContext, data, exception);
        }
    }

    public void next(DefaultContext context, Object data) {
        if(context.invalidated) return;
        if(closeInitiated) return;
        context.invalidated = true;
        DefaultContext newContext = factory.newContext(this);
        try {
            next0(context, data, newContext);
        } catch (OutOfMemoryError | IOError | RuntimeException | InternalException exception) {
            if(newContext.invalidated) return;
            newContext.cause = exception;
            onClose(newContext);
        }
    }

    private void next0(DefaultContext context, Object data, DefaultContext newContext) throws InternalException {
        newContext.channelHandlerMethodName = context.channelHandlerMethodName;
        LinkedList.LinkedNode<ChannelHandler> nextChannelHandlerNode = context.channelHandlerNode.next;
        switch (context.channelHandlerMethodName) {
            case onConnect:
                if(nextChannelHandlerNode == null) break;
                newContext.channelHandlerNode = nextChannelHandlerNode;
                nextChannelHandlerNode.value.onConnect(newContext, data);
                break;
            case onRead:
                if(nextChannelHandlerNode == null) break;
                newContext.channelHandlerNode = nextChannelHandlerNode;
                nextChannelHandlerNode.value.onRead(newContext, data);
                break;
            case onReadComplete:
                if(nextChannelHandlerNode == null) break;
                newContext.channelHandlerNode = nextChannelHandlerNode;
                nextChannelHandlerNode.value.onReadComplete(newContext, data);
                break;
            case onWrite:
                LinkedList.LinkedNode<ChannelHandler> prevChannelHandlerNode = context.channelHandlerNode.prev;
                if(prevChannelHandlerNode == null) {
                    channelOperations.write(data);
                    break;
                }
                newContext.channelHandlerNode = prevChannelHandlerNode;
                prevChannelHandlerNode.value.onWrite(newContext, data);
                break;
        }
    }

    public void write(DefaultContext context, Object data) throws InternalException {
        if(context.invalidated) return;
        if(closeInitiated) throw new InternalException(InternalException.CHANNEL_CLOSED);
        LinkedList.LinkedNode<ChannelHandler> prevChannelHandlerNode = context.channelHandlerNode.prev;
        DefaultContext newContext = factory.newContext(this, prevChannelHandlerNode, ChannelHandlerMethodName.onWrite);
        try {
            if(prevChannelHandlerNode != null) {
                newContext.channelHandlerNode.value.onWrite(newContext, data);
            } else {
                channelOperations.write(data);
            }
        } catch (OutOfMemoryError | InternalException | RuntimeException exception) {
            if(prevChannelHandlerNode != null) context = newContext;
            if(context.invalidated) return;
            context.cause = exception;
            onClose(context);
        }
    }

    public void fireReadInterest(DefaultContext context) throws InternalException {
        if(context.invalidated) return;
        if(closeInitiated) throw new InternalException(InternalException.CHANNEL_CLOSED);
        try {
            channelOperations.setReadInterest();
        } catch (IllegalArgumentException | CancelledKeyException exception) {
            throw new InternalException(exception);
        }
    }

    public void clearReadInterest(DefaultContext context) throws InternalException {
        if(context.invalidated) return;
        if(closeInitiated) throw new InternalException(InternalException.CHANNEL_CLOSED);
        try {
            channelOperations.clearReadInterest();
        } catch (IllegalArgumentException | CancelledKeyException exception) {
            throw new InternalException(exception);
        }
    }

    public void flush(DefaultContext context) throws InternalException {
        if(context.invalidated) return;
        if(closeInitiated) throw new InternalException(InternalException.CHANNEL_CLOSED);
        try {
            channelOperations.flush();
        } catch (OutOfMemoryError | IOError | IOException | RuntimeException exception) {
            throw new InternalException(exception);
        }
    }

    public void close(DefaultContext context) throws InternalException {
        if(context.invalidated) return;
        if(closeInitiated) throw new InternalException(InternalException.CHANNEL_CLOSED);
        onClose(context);
    }
}
