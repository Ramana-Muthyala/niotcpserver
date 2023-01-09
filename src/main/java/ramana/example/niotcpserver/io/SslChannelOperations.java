package ramana.example.niotcpserver.io;

import ramana.example.niotcpserver.types.InternalException;
import ramana.example.niotcpserver.util.Constants;
import ramana.example.niotcpserver.util.SslContextUtil;

import javax.net.ssl.*;
import java.io.IOError;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.logging.Level;

public class SslChannelOperations extends ChannelOperations {
    private final SSLSession session;
    private static final int padding = 1024;
    private final SSLEngine sslEngine;
    static {
        closeTimeout = Constants.SSL_CHANNEL_CLOSE_TIMEOUT;
    }
    private final SslWrapper sslWrapper;
    private final Allocator.Resource<ByteBuffer> netReadResource;
    private final Allocator<ByteBuffer> directAllocator;
    private WrapTask pendingTask;
    private Runnable onConnect;
    private boolean onConnectInvoked;
    private boolean inboundClosed;

    public SslChannelOperations(Allocator<ByteBuffer> allocator, Allocator<ByteBuffer> directAllocator, SelectionKey sk, SslMode sslMode) throws SslContextUtil.SSLContextException, IOException {
        super(allocator, sk);
        this.directAllocator = directAllocator;
        InetSocketAddress socketAddress = (InetSocketAddress) ((SocketChannel)sk.channel()).getRemoteAddress();
        SSLContext sslContext  =  sslMode == SslMode.CLIENT_DEFAULT_TM ? SslContextUtil.getContextWithDefaultTrustManagerFactory() : SslContextUtil.getContext();
        sslEngine = sslContext.createSSLEngine(socketAddress.getHostName(), socketAddress.getPort());
        sslEngine.setUseClientMode(sslMode != SslMode.SERVER);
        session = sslEngine.getSession();
        netReadResource = directAllocator.allocate(session.getPacketBufferSize());
        sslWrapper = new SslWrapper();
    }

    private boolean flushData(ByteBuffer destinationBuffer) throws IOException {
        destinationBuffer.flip();
        while(destinationBuffer.hasRemaining()) {
            int bytesWritten = channel.write(destinationBuffer);
            if(bytesWritten == 0) {
                destinationBuffer.compact();
                setWriteInterest();
                return false;
            }
        }
        clearWriteInterest();
        destinationBuffer.clear();
        return true;
    }

    public void beginHandShake() throws IOException {
        try {
            sslWrapper.wrap();
        } catch (InternalException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void flush() throws IOException, InternalException {
        boolean notTimedOut = closeTicker <= 0 || (System.currentTimeMillis() - closeTicker) < closeTimeout;
        if(notTimedOut) {
            if(pendingTask != null) {
                boolean flushed = flushData(pendingTask.destination.get());
                if(!flushed) return;
                sslWrapper.processPendingTask();
                if(pendingTask != null) return;
            }
            Allocator.Resource<ByteBuffer> resource;
            while ((resource = outboundQueue.peek()) != null) {
                if(!resource.get().hasRemaining()) {
                    outboundQueue.poll();
                    resource.release();
                    continue;
                }
                WrapTask wrapTask = sslWrapper.wrap(resource);
                if(wrapTask.result == WrapTaskStatus.WRAPPED_AND_FLUSHED) {
                    outboundQueue.poll();
                    resource.release();
                    continue;
                }
                break;
            }
        }
        if(closeTicker > 0) close(false);
    }

    @Override
    public void close(boolean forceClose) {
        if(isClosed()) return;
        boolean notTimedOut = closeTicker <= 0 || (System.currentTimeMillis() - closeTicker) < closeTimeout;
        if(!forceClose  &&  notTimedOut  &&  outboundQueue.size() != 0) {
            return;
        }
        releaseResources();
        try {
            WrapTask wrapTask = sslWrapper.close();
            if(wrapTask != null  &&  wrapTask.result == WrapTaskStatus.IN_PROGRESS  &&  notTimedOut) {
                return;
            }
            if(!inboundClosed  &&  sslEngine.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_UNWRAP
                    &&  notTimedOut) {
                return;
            }
            netReadResource.release();
            sslWrapper.appWriteResource.release();
            session.invalidate();
            channel.close();
        } catch (OutOfMemoryError | IOError | IOException exception) {
            logger.log(Level.INFO, exception.getMessage(), exception);
        }
    }

    @Override
    public Object read() throws IOException, InternalException {
        int bytesRead = channel.read(netReadResource.get());
        if(bytesRead == -1) {
            inboundClosed = true;
            return -1;
        }
        if(bytesRead == 0) {
            return 0;
        }
        return sslWrapper.unwrap(netReadResource);
    }

    private class SslWrapper {
        private final Allocator.Resource<ByteBuffer> appWriteResource;
        private SslWrapper() {
            appWriteResource = allocator.allocate(session.getApplicationBufferSize() + padding);
        }

        public void processPendingTask() throws IOException, InternalException {
            try {
                pendingTask.process();
            } catch (SSLException exception) {
                channel.close();
                throw exception;
            }
            if(pendingTask.result == WrapTaskStatus.IN_PROGRESS) {
                return;
            }
            WrapTask wrapTask = pendingTask;
            pendingTask = null;
            if(wrapTask.source == appWriteResource) {
                appWriteResource.get().clear();
            }
            if(!onConnectInvoked  &&  wrapTask.handShakeStatus == SSLEngineResult.HandshakeStatus.FINISHED) {
                onConnectInvoked = true;
                onConnect.run();
            }
            pendingTask = null;
        }

        private void wrap() throws IOException, InternalException {
            appWriteResource.get().flip();
            wrap(appWriteResource);
        }

        private WrapTask wrap(Allocator.Resource<ByteBuffer> source) throws InternalException, IOException {
            WrapTask wrapTask = new WrapTask(source);
            try {
                wrapTask.process();
            } catch (SSLException exception) {
                channel.close();
                throw exception;
            }
            if(wrapTask.result == WrapTaskStatus.IN_PROGRESS) {
                pendingTask = wrapTask;
                return wrapTask;
            }
            if(wrapTask.source == appWriteResource) {
                appWriteResource.get().clear();
            }
            if(!onConnectInvoked  &&  wrapTask.handShakeStatus == SSLEngineResult.HandshakeStatus.FINISHED) {
                onConnectInvoked = true;
                onConnect.run();
            }
            return wrapTask;
        }

        private Object unwrap(Allocator.Resource<ByteBuffer> netReadResource) throws IOException, InternalException {
            ByteBuffer sourceBuffer = netReadResource.get();
            sourceBuffer.flip();
            UnwrapTask unWrapTask = new UnwrapTask(netReadResource);
            try {
                unWrapTask.process();
            } catch (SSLException exception) {
                channel.close();
                throw exception;
            }
            SSLEngineResult sslEngineResult = unWrapTask.sslEngineResult;
            if(sslEngineResult.getStatus() == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
                sourceBuffer.compact();
            } else {
                sourceBuffer.clear();
                if(sslEngine.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_WRAP) {
                    wrap();
                } else {
                    if(sslEngineResult.getStatus() == SSLEngineResult.Status.CLOSED
                            &&  sslEngineResult.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
                        sslEngine.closeInbound();
                        SslChannelOperations.this.close(true);
                    }
                }
            }
            if(!onConnectInvoked  &&  unWrapTask.handShakeStatus == SSLEngineResult.HandshakeStatus.FINISHED) {
                onConnectInvoked = true;
                onConnect.run();
            }
            return unWrapTask.result;
        }

        public WrapTask close() {
            try {
                sslEngine.closeOutbound();
                while(!sslEngine.isOutboundDone()) {
                    appWriteResource.get().flip();
                    WrapTask wrapTask = wrap(appWriteResource);
                    if(wrapTask.result == WrapTaskStatus.IN_PROGRESS  ||
                            wrapTask.sslEngineResult.getHandshakeStatus() != SSLEngineResult.HandshakeStatus.NEED_WRAP) {
                        return wrapTask;
                    }
                }
            } catch (InternalException | IOException exception) {
                logger.log(Level.INFO, exception.getMessage(), exception);
            }
            return null;
        }
    }

    private abstract class Task {
        protected final Allocator.Resource<ByteBuffer> source;
        protected Allocator.Resource<ByteBuffer> destination;
        protected SSLEngineResult sslEngineResult;
        protected SSLEngineResult.HandshakeStatus handShakeStatus;
        protected Object result;

        protected Task(Allocator.Resource<ByteBuffer> source, Allocator.Resource<ByteBuffer> destination) {
            this.source = source;
            this.destination = destination;
        }

        protected void runDelegated() {
            Runnable runnable;
            while ((runnable = sslEngine.getDelegatedTask()) != null) {
                runnable.run();
            }
        }

        protected abstract void process() throws IOException, InternalException;
    }

    private class UnwrapTask extends Task {
        protected UnwrapTask(Allocator.Resource<ByteBuffer> source) {
            super(source, allocator.allocate(session.getApplicationBufferSize() + padding));
        }

        @Override
        protected void process() throws IOException, InternalException {
            ByteBuffer sourceBuffer = source.get();
            ByteBuffer destinationBuffer = destination.get();
            ArrayList<Allocator.Resource<ByteBuffer>> overflows = new ArrayList<>();
            sslEngineResult = sslEngine.unwrap(sourceBuffer, destinationBuffer);
            SSLEngineResult.Status resultStatus = sslEngineResult.getStatus();
            while(sslEngineResult.bytesProduced() > 0
                    ||  resultStatus == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                if(resultStatus == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                    destinationBuffer.flip();
                    overflows.add(destination);
                    destination = allocator.allocate(session.getApplicationBufferSize() + padding);
                    destinationBuffer = destination.get();
                }
                if(sourceBuffer.hasRemaining()) {
                    sslEngineResult = sslEngine.unwrap(sourceBuffer, destinationBuffer);
                    resultStatus = sslEngineResult.getStatus();
                } else {
                    break;
                }
            }

            if(sslEngineResult.bytesProduced() > 0) {
                if(overflows.size() == 0) {
                    result = destination;
                } else {
                    destinationBuffer.flip();
                    Allocator.Resource<ByteBuffer> resource = allocator.allocate(destinationBuffer.limit() + getOverflowCapacity(overflows));
                    ByteBuffer byteBuffer = resource.get();
                    addOverflows(overflows, byteBuffer);
                    byteBuffer.put(destinationBuffer);
                    destination.release();
                    result = resource;
                }
                return;
            }

            if(overflows.size() > 0) {
                Allocator.Resource<ByteBuffer> resource = allocator.allocate(getOverflowCapacity(overflows));
                ByteBuffer byteBuffer = resource.get();
                addOverflows(overflows, byteBuffer);
                destination.release();
                result = resource;
                return;
            }

            handShakeStatus = sslEngineResult.getHandshakeStatus();
            while(sslEngineResult.bytesConsumed() > 0) {
                if(handShakeStatus == SSLEngineResult.HandshakeStatus.NEED_TASK) {
                    runDelegated();
                    handShakeStatus = sslEngine.getHandshakeStatus();
                }
                if(sourceBuffer.hasRemaining()  &&  handShakeStatus == SSLEngineResult.HandshakeStatus.NEED_UNWRAP) {
                    sslEngineResult = sslEngine.unwrap(sourceBuffer, destinationBuffer);
                    handShakeStatus = sslEngineResult.getHandshakeStatus();
                } else {
                    break;
                }
            }
            destination.release();
            result = 0;
        }

        private void addOverflows(ArrayList<Allocator.Resource<ByteBuffer>> overflows, ByteBuffer byteBuffer) throws InternalException {
            for (Allocator.Resource<ByteBuffer> overflow: overflows) {
                byteBuffer.put(overflow.get());
                overflow.release();
            }
        }

        private int getOverflowCapacity(ArrayList<Allocator.Resource<ByteBuffer>> overflows) throws InternalException {
            int capacity = 0;
            for (Allocator.Resource<ByteBuffer> overflow: overflows) {
                capacity += overflow.get().limit();
            }
            return capacity;
        }
    }

    private class WrapTask extends Task {
        protected WrapTask(Allocator.Resource<ByteBuffer> source) {
            super(source, directAllocator.allocate(session.getPacketBufferSize()));
        }

        @Override
        protected void process() throws IOException, InternalException {
            ByteBuffer sourceBuffer = source.get();
            ByteBuffer destinationBuffer = destination.get();
            sslEngineResult = sslEngine.wrap(sourceBuffer, destinationBuffer);
            while(sslEngineResult.bytesConsumed() > 0) {
                boolean flushed = flushData(destinationBuffer);
                if(!flushed) {
                    result = WrapTaskStatus.IN_PROGRESS;
                    return;
                }
                if(sourceBuffer.hasRemaining()) {
                    sslEngineResult = sslEngine.wrap(sourceBuffer, destinationBuffer);
                } else {
                    destination.release();
                    result = WrapTaskStatus.WRAPPED_AND_FLUSHED;
                    return;
                }
            }

            handShakeStatus = sslEngineResult.getHandshakeStatus();
            while(sslEngineResult.bytesProduced() > 0) {
                boolean flushed = flushData(destinationBuffer);
                if(!flushed) {
                    result = WrapTaskStatus.IN_PROGRESS;
                    return;
                }
                if(handShakeStatus == SSLEngineResult.HandshakeStatus.NEED_TASK) {
                    runDelegated();
                    handShakeStatus = sslEngine.getHandshakeStatus();
                }
                if(handShakeStatus == SSLEngineResult.HandshakeStatus.NEED_WRAP) {
                    sslEngineResult = sslEngine.wrap(sourceBuffer, destinationBuffer);
                    handShakeStatus = sslEngineResult.getHandshakeStatus();
                } else {
                    break;
                }
            }
            destination.release();
            result = WrapTaskStatus.DONE;
        }
    }

    private enum WrapTaskStatus { IN_PROGRESS, DONE, WRAPPED_AND_FLUSHED}

    public void onConnect(Runnable onConnect) {
        this.onConnect = onConnect;
    }
}
