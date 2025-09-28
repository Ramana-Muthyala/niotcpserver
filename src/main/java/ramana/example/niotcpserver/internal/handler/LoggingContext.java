package ramana.example.niotcpserver.internal.handler;

import ramana.example.niotcpserver.handler.ChannelHandler;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.log.LogFactory;
import ramana.example.niotcpserver.types.Event;
import ramana.example.niotcpserver.types.InternalException;
import ramana.example.niotcpserver.types.LinkedList;
import ramana.example.niotcpserver.util.Util;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

public class LoggingContext extends DefaultContext {
    private static final Logger logger = LogFactory.getLogger();
    LoggingContext(InternalChannelHandler internalChannelHandler, LinkedList.LinkedNode<ChannelHandler> channelHandlerNode, Event event) {
        super(internalChannelHandler, channelHandlerNode, event);
    }

    LoggingContext(InternalChannelHandler internalChannelHandler, LinkedList.LinkedNode<ChannelHandler> channelHandlerNode, Throwable cause) {
        super(internalChannelHandler, channelHandlerNode, cause);
    }

    public LoggingContext(InternalChannelHandler internalChannelHandler) {
        super(internalChannelHandler);
    }

    @Override
    public void fireReadInterest() throws InternalException {
        logger.info(getSocketAddress() + " " + Util.normalizeClassName(channelHandlerNode.value.getClass().getName()));
        super.fireReadInterest();
    }

    @Override
    public void clearReadInterest() throws InternalException {
        logger.info(getSocketAddress() + " " + Util.normalizeClassName(channelHandlerNode.value.getClass().getName()));
        super.clearReadInterest();
    }

    @Override
    public void write(Object data) throws InternalException {
        logger.info(getSocketAddress() + " " + Util.normalizeClassName(channelHandlerNode.value.getClass().getName()) + " data: " + Util.toString(data));
        super.write(data);
    }

    @Override
    public void flush() throws InternalException {
        logger.info(getSocketAddress() + " " + Util.normalizeClassName(channelHandlerNode.value.getClass().getName()));
        super.flush();
    }

    @Override
    public Allocator<ByteBuffer> allocator() {
        return super.allocator();
    }

    @Override
    public void close() throws InternalException {
        logger.info(getSocketAddress() + " " + Util.normalizeClassName(channelHandlerNode.value.getClass().getName()));
        super.close();
    }

    @Override
    public void next(Object data) throws InternalException {
        //logger.info(getSocketAddress() + " " + Util.normalizeClassName(channelHandlerNode.value.getClass().getName()) + " data: " + Util.toString(data));
        super.next(data);
    }

    @Override
    public void next(Object data, Throwable cause) throws InternalException {
        //logger.log(Level.INFO, getSocketAddress() + " " + Util.normalizeClassName(channelHandlerNode.value.getClass().getName()) + " data: " + Util.toString(data), cause);
        super.next(data, cause);
    }
}
