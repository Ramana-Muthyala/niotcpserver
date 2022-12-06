package ramana.example.niotcpserver.handler.impl;

import ramana.example.niotcpserver.handler.ChannelHandler;
import ramana.example.niotcpserver.handler.Context;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.log.LogFactory;
import ramana.example.niotcpserver.types.InternalException;
import ramana.example.niotcpserver.util.Util;
import ramana.example.niotcpserver.worker.impl.DefaultContext;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingHandler implements ChannelHandler {
    private static final Logger logger = LogFactory.getLogger();
    public static final LoggingHandler defaultInstance = new LoggingHandler();
    @Override
    public void onConnect(Context.OnConnect context, Object data) throws InternalException {
        logger.info(((DefaultContext) context).getSocketAddress() + " data: " + Util.toString(data));
        context.next(data);
    }

    @Override
    public void onRead(Context.OnRead context, Object data) throws InternalException {
        logger.info(((DefaultContext) context).getSocketAddress() + " data: " + Util.toString(data));
        context.next(data);
    }

    @Override
    public void onReadComplete(Context.OnRead context, Object data) throws InternalException {
        logger.info(((DefaultContext) context).getSocketAddress() + " data: " + Util.toString(data));
        context.next(data);
    }

    @Override
    public void onWrite(Context.OnWrite context, Object data) throws InternalException {
        logger.info(((DefaultContext) context).getSocketAddress() + " data: " + Util.toString(data));
        context.next(data);
    }

    @Override
    public void onClose(Context.OnClose context, Object data, Throwable cause) throws InternalException {
        logger.log(Level.INFO, ((DefaultContext) context).getSocketAddress() + " data: " + Util.toString(data), cause);
        context.next(data, cause);
    }
}
