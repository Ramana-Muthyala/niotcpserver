package ramana.example.niotcpserver.worker.impl;

import ramana.example.niotcpserver.handler.ChannelHandler;
import ramana.example.niotcpserver.types.ChannelHandlerMethodName;
import ramana.example.niotcpserver.types.LinkedList;

public class ContextFactory {
    public static ContextFactory factory() {
        return new ContextFactory();
    }

    public static ContextFactory loggingFactory() {
        return new LoggingContextFactory();
    }

    DefaultContext newContext(InternalChannelHandler internalChannelHandler, LinkedList.LinkedNode<ChannelHandler> channelHandlerNode, ChannelHandlerMethodName channelHandlerMethodName) {
        return new DefaultContext(internalChannelHandler, channelHandlerNode, channelHandlerMethodName);
    }

    DefaultContext newContext(InternalChannelHandler internalChannelHandler, LinkedList.LinkedNode<ChannelHandler> channelHandlerNode, Throwable cause) {
        return new DefaultContext(internalChannelHandler, channelHandlerNode, cause);
    }

    DefaultContext newContext(InternalChannelHandler internalChannelHandler){
        return new DefaultContext(internalChannelHandler);
    }

    static class LoggingContextFactory extends ContextFactory {
        @Override
        DefaultContext newContext(InternalChannelHandler internalChannelHandler, LinkedList.LinkedNode<ChannelHandler> channelHandlerNode, ChannelHandlerMethodName channelHandlerMethodName) {
            return new LoggingContext(internalChannelHandler, channelHandlerNode, channelHandlerMethodName);
        }

        @Override
        DefaultContext newContext(InternalChannelHandler internalChannelHandler, LinkedList.LinkedNode<ChannelHandler> channelHandlerNode, Throwable cause) {
            return new LoggingContext(internalChannelHandler, channelHandlerNode, cause);
        }

        @Override
        DefaultContext newContext(InternalChannelHandler internalChannelHandler) {
            return new LoggingContext(internalChannelHandler);
        }
    }
}
