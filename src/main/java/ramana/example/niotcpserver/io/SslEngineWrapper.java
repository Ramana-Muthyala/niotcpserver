package ramana.example.niotcpserver.io;

import ramana.example.niotcpserver.types.InternalException;
import ramana.example.niotcpserver.util.Constants;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class SslEngineWrapper {
    private static final long closeTimeout = Constants.SSL_CHANNEL_CLOSE_TIMEOUT;
    private static final int WRITE_TO_CHANNEL_INCOMPLETE = 1;
    private final SSLEngine sslEngine;
    private final SSLSession session;
    private final Allocator<ByteBuffer> allocator;
    private final Allocator<ByteBuffer> directAllocator;
    private final SslChannelOperations channelOperations;
    private Long closeInitiatedAt;
    private boolean closed;
    public boolean gracefulCloseInitiated;

    public SslEngineWrapper(SSLEngine sslEngine, SslChannelOperations channelOperations, Allocator<ByteBuffer> allocator, Allocator<ByteBuffer> directAllocator) {
        this.sslEngine = sslEngine;
        this.channelOperations = channelOperations;
        this.session = sslEngine.getSession();
        this.allocator = allocator;
        this.directAllocator = directAllocator;
    }

    public Result read(Allocator.Resource<ByteBuffer> source) throws IOException, InternalException {
        source.get().flip();
        Result result = new UnwrapTask(source).process();
        if(sslEngine.isInboundDone()) {
            sslEngine.closeInbound();
            Allocator.Resource<ByteBuffer> tmpSource = allocator.allocate(Constants.WRAP_TASK_BUFFER_CAPACITY_FOR_NO_SOURCE_DATA);
            tmpSource.get().flip();
            new WrapTask(tmpSource).process();
            initiateClose();
            gracefulClose();
            if(closed) {
                // mark value as end of stream as connection is closed.
                result.value = -1;
            }
        }
        return result;
    }

    public Result write(Allocator.Resource<ByteBuffer> source) throws IOException, InternalException {
        return new WrapTask(source).process();
    }

    public void initiateClose() {
        if(closeInitiatedAt == null) {
            closeInitiatedAt = System.currentTimeMillis();
        }
    }

    public void gracefulClose() throws IOException, InternalException {
        gracefulCloseInitiated = true;
        sslEngine.closeOutbound();
        while(!sslEngine.isOutboundDone()) {
            Allocator.Resource<ByteBuffer> tmpSource = allocator.allocate(Constants.WRAP_TASK_BUFFER_CAPACITY_FOR_NO_SOURCE_DATA);
            tmpSource.get().flip();
            Result result = new WrapTask(tmpSource).process();
            if(result.value instanceof Integer) {
                int tmpInt = (int) result.value;
                if(tmpInt == WRITE_TO_CHANNEL_INCOMPLETE) {
                    return;
                }
            }
        }
        close();
    }

    public void close() {
        session.invalidate();
        channelOperations.close0();
        closed = true;
    }

    public void closeIfInitiated() throws IOException, InternalException {
        if(closed  ||  (closeInitiatedAt == null)) {
            return;
        }
        if((System.currentTimeMillis() - closeInitiatedAt) > closeTimeout) {
            close();
            return;
        }
        gracefulClose();
    }

    public static class Result {
        public Object value = 0;
        public boolean handshakeFinished;
    }

    public abstract class Task {
        protected final Allocator.Resource<ByteBuffer> source;
        protected Task(Allocator.Resource<ByteBuffer> source) {
            this.source = source;
        }

        protected void runDelegated() {
            Runnable runnable;
            while ((runnable = sslEngine.getDelegatedTask()) != null) {
                runnable.run();
            }
        }

        protected abstract Result process() throws IOException, InternalException;
    }

    private class WrapTask extends Task {
        protected WrapTask(Allocator.Resource<ByteBuffer> source) {
            super(source);
        }

        @Override
        protected Result process() throws IOException, InternalException {
            Result result = new Result();
            Allocator.Resource<ByteBuffer> destination = null;
            ByteBuffer destinationBuffer = null;
            while(true) {
                if(destination == null) {
                    destination = directAllocator.allocate(session.getPacketBufferSize());
                    destinationBuffer = destination.get();
                } else {
                    destinationBuffer.clear();
                }
                ByteBuffer sourceBuffer = source.get();
                SSLEngineResult sslEngineResult = sslEngine.wrap(sourceBuffer, destinationBuffer);
                SSLEngineResult.HandshakeStatus handShakeStatus = sslEngineResult.getHandshakeStatus();
                if(sslEngineResult.bytesProduced() > 0) {
                    if(!channelOperations.writeToChannel(destinationBuffer)) {
                        result.value = WRITE_TO_CHANNEL_INCOMPLETE;
                        return result;
                    }
                    if(sourceBuffer.hasRemaining()  &&  sslEngineResult.bytesConsumed() > 0) {
                        // Not in handshaking mode.
                        continue;
                    }
                }
                if(handShakeStatus == SSLEngineResult.HandshakeStatus.NEED_TASK) {
                    runDelegated();
                    handShakeStatus = sslEngine.getHandshakeStatus();
                }
                if(handShakeStatus == SSLEngineResult.HandshakeStatus.FINISHED) {
                    result.handshakeFinished = true;
                }
                if(handShakeStatus == SSLEngineResult.HandshakeStatus.NEED_WRAP) {
                    continue;
                }
                if(handShakeStatus == SSLEngineResult.HandshakeStatus.NEED_UNWRAP) {
                    destination.release();
                    return result;
                }
                if(sourceBuffer.hasRemaining()) {
                    // Case where handshaking just completed without consuming any bytes in source buffer
                    continue;
                }
                destination.release();
                return result;
            }
        }
    }

    private class UnwrapTask extends Task {
        protected UnwrapTask(Allocator.Resource<ByteBuffer> source) {
            super(source);
        }

        @Override
        protected Result process() throws IOException, InternalException {
            Result result = new Result();
            Allocator.Resource<ByteBuffer> destination = allocator.allocate(session.getApplicationBufferSize());
            ByteBuffer destinationBuffer = destination.get();
            ByteBuffer sourceBuffer = source.get();
            ArrayList<Allocator.Resource<ByteBuffer>> accumulator = new ArrayList<>();
            while(sourceBuffer.hasRemaining()) {
                SSLEngineResult sslEngineResult = sslEngine.unwrap(sourceBuffer, destinationBuffer);
                SSLEngineResult.Status resultStatus = sslEngineResult.getStatus();
                SSLEngineResult.HandshakeStatus handShakeStatus = sslEngineResult.getHandshakeStatus();
                if(resultStatus == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
                    result.value = copy(accumulator, destination);
                    sourceBuffer.compact();
                    return result;
                }
                if(resultStatus == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                    if(destinationBuffer.position() == 0) {
                        // can an overflow happen without writing to destination ?
                        throw new AssertionError();
                    }
                }
                if(handShakeStatus == SSLEngineResult.HandshakeStatus.NEED_TASK) {
                    runDelegated();
                    handShakeStatus = sslEngine.getHandshakeStatus();
                }
                if(handShakeStatus == SSLEngineResult.HandshakeStatus.FINISHED) {
                    result.handshakeFinished = true;
                }
                if(handShakeStatus == SSLEngineResult.HandshakeStatus.NEED_UNWRAP) {
                    if(sslEngineResult.bytesProduced() > 0  ||  destinationBuffer.position() != 0) {
                        // bytes can be consumed but not produced as it is in handshaking mode.
                        throw new AssertionError();
                    }
                    continue;
                }
                if(handShakeStatus == SSLEngineResult.HandshakeStatus.NEED_WRAP) {
                    Allocator.Resource<ByteBuffer> tmpSource = allocator.allocate(Constants.WRAP_TASK_BUFFER_CAPACITY_FOR_NO_SOURCE_DATA);
                    tmpSource.get().flip();
                    Result tmpResult = new WrapTask(tmpSource).process();
                    if(tmpResult.handshakeFinished) {
                        // mark handshake as finished as it is done in this context.
                        result.handshakeFinished = true;
                    }
                }
                if(destinationBuffer.position() > 0) {
                    // Not in handshaking mode. Either overflow or normal unwrap.
                    destinationBuffer.flip();
                    accumulator.add(destination);
                    destination = allocator.allocate(session.getApplicationBufferSize());
                    destinationBuffer = destination.get();
                }
            }
            result.value = copy(accumulator, destination);
            sourceBuffer.clear();
            return result;
        }

        private Object copy(ArrayList<Allocator.Resource<ByteBuffer>> accumulator, Allocator.Resource<ByteBuffer> destination) throws InternalException {
            int capacity = 0;
            for (Allocator.Resource<ByteBuffer> tmpResource: accumulator) {
                capacity += tmpResource.get().limit();
            }
            ByteBuffer destinationBuffer = destination.get();
            if(capacity == 0  &&  destinationBuffer.position() > 0) {
                return destination;
            }
            destinationBuffer.flip();
            capacity += destinationBuffer.limit();
            if(capacity == 0) return 0;
            Allocator.Resource<ByteBuffer> resultResource = allocator.allocate(capacity);
            ByteBuffer resultBuffer = resultResource.get();
            for (Allocator.Resource<ByteBuffer> tmpResource: accumulator) {
                resultBuffer.put(tmpResource.get());
                tmpResource.release();
            }
            resultBuffer.put(destinationBuffer);
            destination.release();
            return resultResource;
        }
    }
}
