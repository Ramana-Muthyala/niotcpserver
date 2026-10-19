package ramana.example.niotcpserver.io;

import ramana.example.niotcpserver.types.InternalException;
import ramana.example.niotcpserver.util.Constants;
import ramana.example.niotcpserver.util.SslContextUtil;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.IOError;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;

public class SslChannelOperations extends ChannelOperations {
    private ByteBuffer pendingWrite;
    private final SslEngineWrapper sslEngineWrapper;
    private final Allocator.Resource<ByteBuffer> netReadResource;
    private Runnable onConnect;
    private boolean onConnectCalled;

    public SslChannelOperations(Allocator<ByteBuffer> allocator, Allocator<ByteBuffer> directAllocator, SelectionKey sk, SslMode sslMode) throws SslContextUtil.SSLContextException, IOException {
        super(allocator, sk);
        InetSocketAddress socketAddress = (InetSocketAddress) ((SocketChannel)sk.channel()).getRemoteAddress();
        SSLContext sslContext  =  sslMode == SslMode.CLIENT_DEFAULT_TM ? SslContextUtil.getContextWithDefaultTrustManagerFactory() : SslContextUtil.getContext();
        SSLEngine sslEngine = sslContext.createSSLEngine(socketAddress.getHostName(), socketAddress.getPort());
        sslEngine.setUseClientMode(sslMode != SslMode.SERVER);
        netReadResource = directAllocator.allocate(sslEngine.getSession().getPacketBufferSize());
        sslEngineWrapper = new SslEngineWrapper(sslEngine, this, allocator, directAllocator);
    }

    public boolean writeToChannel(ByteBuffer byteBuffer) throws IOException {
        if(byteBuffer == null) return true;

        byteBuffer.flip();
        while(byteBuffer.hasRemaining()) {
            int bytesWritten = channel.write(byteBuffer);
            if(bytesWritten == 0) {
                byteBuffer.compact();
                pendingWrite = byteBuffer;
                setWriteInterest();
                return false;
            }
        }
        pendingWrite = null;
        return true;
    }

    public void beginHandShake() throws IOException {
        try {
            Allocator.Resource<ByteBuffer> tmpSource = allocator.allocate(Constants.WRAP_TASK_BUFFER_CAPACITY_FOR_NO_SOURCE_DATA);
            tmpSource.get().flip();
            SslEngineWrapper.Result result = sslEngineWrapper.write(tmpSource);
            invokeOnConnect(result);
        } catch (InternalException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void flushAndClose() {
        try {
            sslEngineWrapper.initiateClose();
            flush();
        } catch (OutOfMemoryError | IOError | IOException | RuntimeException | InternalException exception) {
            logger.log(Level.INFO, exception.getMessage(), exception);
            close();
        }
    }

    @Override
    public void flush() throws IOException, InternalException {
        if(!writeToChannel(pendingWrite)) return;
        if(sslEngineWrapper.gracefulCloseInitiated) {
            sslEngineWrapper.closeIfInitiated();
            return;
        }

        Allocator.Resource<ByteBuffer> resource;
        while((resource = outboundQueue.peek()) != null) {
            if(!resource.get().hasRemaining()) {
                outboundQueue.poll();
                resource.release();
                continue;
            }
            SslEngineWrapper.Result result = sslEngineWrapper.write(resource);
            invokeOnConnect(result);
            if(resource.get().hasRemaining()) {
                setWriteInterest();
                return;
            }
            outboundQueue.poll();
            resource.release();
        }
        if(outboundQueue.isEmpty()) clearWriteInterest();

        sslEngineWrapper.closeIfInitiated();
    }

    @Override
    public void close() {
        if(isClosed()) return;
        sslEngineWrapper.close();
    }

    public void close0() {
        netReadResource.release();
        super.close();
    }

    @Override
    public Object read() throws IOException, InternalException {
        int bytesRead = channel.read(netReadResource.get());
        if(bytesRead > 0) {
            SslEngineWrapper.Result result = sslEngineWrapper.read(netReadResource);
            invokeOnConnect(result);
            return result.value;
        }
        return bytesRead;
    }

    private void invokeOnConnect(SslEngineWrapper.Result result) {
        if(result.handshakeFinished  &&  (!onConnectCalled)) {
            onConnectCalled = true;
            onConnect.run();
        }
    }

    public void onConnect(Runnable onConnect) {
        this.onConnect = onConnect;
    }
}
