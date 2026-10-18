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
    protected static final Logger logger = LogFactory.getLogger();
    public final SelectionKey selectionKey;
    public final SocketChannel channel;
    protected final Allocator<ByteBuffer> allocator;
    protected final Queue<Allocator.Resource<ByteBuffer>> outboundQueue = new LinkedList<>();
    private Long closeInitiatedAt;
    protected static long closeTimeout = Constants.CHANNEL_CLOSE_TIMEOUT;

    public ChannelOperations(Allocator<ByteBuffer> allocator, SelectionKey sk) {
        this.allocator = allocator;
        this.selectionKey = sk;
        this.channel = (SocketChannel) sk.channel();
    }

    public void flushAndClose() {
        try {
            initiateClose();
            flush();
        } catch (OutOfMemoryError | IOError | IOException | RuntimeException | InternalException exception) {
            logger.log(Level.INFO, exception.getMessage(), exception);
            close();
        }
    }

    public boolean isClosed() {
        return !channel.isOpen();
    }

    public void close() {
        if(isClosed()) return;
        releaseResources();
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
        if(outboundQueue.isEmpty()) clearWriteInterest();

        closeIfInitiated();
    }

    public void closeIfInitiated() {
        if(isClosed()  ||  closeInitiatedAt == null) return;
        if(outboundQueue.isEmpty()  ||  (System.currentTimeMillis() - closeInitiatedAt) > closeTimeout) {
            close();
        }
    }

    public void initiateClose() {
        if(closeInitiatedAt == null) {
            closeInitiatedAt = System.currentTimeMillis();
        }
    }

    protected void releaseResources() {
        Allocator.Resource<ByteBuffer> resource;
        while ((resource = outboundQueue.poll()) != null) {
            resource.release();
        }
    }

    protected void setWriteInterest() {
        if((selectionKey.interestOps() & SelectionKey.OP_WRITE) == Constants.SELECTION_KEY_OP_NONE)
            selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
    }

    protected void clearWriteInterest() {
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
