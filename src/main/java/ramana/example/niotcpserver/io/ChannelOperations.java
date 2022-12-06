package ramana.example.niotcpserver.io;

import ramana.example.niotcpserver.log.LogFactory;
import ramana.example.niotcpserver.types.InternalException;
import ramana.example.niotcpserver.util.Constants;

import java.io.IOError;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChannelOperations {
    private static final Logger logger = LogFactory.getLogger();
    public final SelectionKey selectionKey;
    public final SocketChannel channel;
    private final Allocator<ByteBuffer> allocator;
    private final Queue<Allocator.Resource<ByteBuffer>> outboundQueue = new LinkedList<>();
    private long closeTicker;
    private static final long closeTimeout = Constants.CHANNEL_CLOSE_TIMEOUT;

    public ChannelOperations(Allocator<ByteBuffer> allocator, SelectionKey sk) {
        this.allocator = allocator;
        this.selectionKey = sk;
        this.channel = (SocketChannel) sk.channel();
    }

    public void flushAndClose() {
        closeTicker = System.currentTimeMillis();
        try {
            flush();
        } catch (OutOfMemoryError | IOError | IOException | RuntimeException | InternalException exception) {
            logger.log(Level.INFO, exception.getMessage(), exception);
            close(true);
        }
    }

    public boolean isClosed() {
        return !channel.isOpen();
    }

    public void close(boolean forceClose) {
        if(isClosed()) return;
        if(!forceClose  &&  outboundQueue.size() != 0
                &&  (System.currentTimeMillis() - closeTicker) < closeTimeout) return;
        releaseResources();
        try {
            channel.shutdownOutput();
        } catch (OutOfMemoryError | IOError | IOException exception) {
            logger.log(Level.INFO, exception.getMessage(), exception);
        }
        try {
            channel.close();
        } catch (OutOfMemoryError | IOError | IOException exception) {
            logger.log(Level.INFO, exception.getMessage(), exception);
        }
    }

    public void write(Object data) throws InternalException {
        if(!(data instanceof Allocator.Resource)) throw new InternalException(InternalException.ILLEGAL_ARGUMENT_FOR_WRITE + (data == null ? null : data.getClass().getName()));
        Allocator.Resource resource = (Allocator.Resource) data;
        Object tmp = resource.get();
        if(!(tmp instanceof ByteBuffer)) throw new InternalException(InternalException.ILLEGAL_ARGUMENT_FOR_WRITE + (tmp == null ? null : data.getClass().getName() + "<" + tmp.getClass().getName() + ">"));
        ByteBuffer byteBuffer = (ByteBuffer) tmp;
        byteBuffer.flip();
        outboundQueue.offer(resource);
    }

    public void flush() throws IOException, InternalException {
        Allocator.Resource<ByteBuffer> resource;
        while ((resource = outboundQueue.peek()) != null) {
            ByteBuffer byteBuffer = resource.get();
            channel.write(byteBuffer);
            if(byteBuffer.hasRemaining()) {
                setWriteInterest();
                break;
            } else {
                outboundQueue.poll();
                resource.release();
            }
        }
        if(outboundQueue.size() == 0) clearWriteInterest();
        if(closeTicker > 0) close(false);
    }

    private void releaseResources() {
        Allocator.Resource<ByteBuffer> resource;
        while ((resource = outboundQueue.poll()) != null) {
            resource.release();
        }
    }

    private void setWriteInterest() {
        if((selectionKey.interestOps() & SelectionKey.OP_WRITE) == Constants.SELECTION_KEY_OP_NONE)
            selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
    }

    private void clearWriteInterest() {
        if((selectionKey.interestOps() & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE)
            selectionKey.interestOps(selectionKey.interestOps() & (~SelectionKey.OP_WRITE));
    }

    public void setReadInterest() {
        if((selectionKey.interestOps() & SelectionKey.OP_READ) == Constants.SELECTION_KEY_OP_NONE)
            selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_READ);
    }

    public void clearReadInterest() {
        if((selectionKey.interestOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ)
            selectionKey.interestOps(selectionKey.interestOps() & (~SelectionKey.OP_READ));
    }

    public Object read() throws IOException, InternalException {
        Allocator.Resource<ByteBuffer> resource = allocator.allocate(Constants.READ_BUFFER_CAPACITY);
        int bytesRead = channel.read(resource.get());
        if(bytesRead == -1  ||  bytesRead == 0) {
            resource.release();
            return bytesRead;
        }
        return resource;
    }
}
