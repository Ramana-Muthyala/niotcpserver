package ramana.example.niotcpserver.worker.impl;

import ramana.example.niotcpserver.handler.ChannelHandler;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.io.SslMode;
import ramana.example.niotcpserver.types.InternalException;
import ramana.example.niotcpserver.types.LinkedList;
import ramana.example.niotcpserver.util.SslContextUtil;
import ramana.example.niotcpserver.worker.Worker;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public class IdleTimeoutInternalChannelHandler extends InternalChannelHandler {
    private final int idleTimeoutInMilliSec;
    private final Worker worker;
    private final boolean loggingEnabled;
    private long idleTimeoutTicker;

    public IdleTimeoutInternalChannelHandler(LinkedList<ChannelHandler> channelHandlers, ContextFactory factory, boolean defaultRead, Worker worker, int idleTimeout, boolean loggingEnabled) {
        super(channelHandlers, worker.allocator, factory, defaultRead);
        this.worker = worker;
        idleTimeoutInMilliSec = idleTimeout * 1000;
        this.loggingEnabled = loggingEnabled;
    }

    private void scheduleIdleTimeoutTask() {
        idleTimeoutTicker = System.currentTimeMillis();
        final long futureTime = idleTimeoutTicker + idleTimeoutInMilliSec;
        Runnable task = () -> {
            if(channelOperations.isClosed()) return;
            if(futureTime - idleTimeoutTicker == idleTimeoutInMilliSec) {
                if(loggingEnabled) {
                    logger.info(getSocketAddress() + " Timed out: Closing channel");
                }
                channelOperations.flushAndClose();
            }
        };
        worker.schedule(task, futureTime);
    }

    @Override
    public void handleWrite() {
        super.handleWrite();
        scheduleIdleTimeoutTask();
    }

    @Override
    public void handleRead() {
        super.handleRead();
        scheduleIdleTimeoutTask();
    }

    @Override
    public void onConnect(SelectionKey sk, SslMode sslMode, Allocator<ByteBuffer> directAllocator) throws SslContextUtil.SSLContextException, IOException {
        super.onConnect(sk, sslMode, directAllocator);
        scheduleIdleTimeoutTask();
    }

    @Override
    public void fireReadInterest(DefaultContext context) throws InternalException {
        super.fireReadInterest(context);
        idleTimeoutTicker = System.currentTimeMillis();
    }

    @Override
    public void clearReadInterest(DefaultContext context) throws InternalException {
        super.clearReadInterest(context);
        idleTimeoutTicker = System.currentTimeMillis();
    }

    @Override
    public void write(DefaultContext context, Object data) throws InternalException {
        super.write(context, data);
        idleTimeoutTicker = System.currentTimeMillis();
    }

    @Override
    public void flush(DefaultContext context) throws InternalException {
        super.flush(context);
        idleTimeoutTicker = System.currentTimeMillis();
    }
}
