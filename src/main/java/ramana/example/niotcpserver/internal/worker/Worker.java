package ramana.example.niotcpserver.internal.worker;

import ramana.example.niotcpserver.Server;
import ramana.example.niotcpserver.conf.Configuration;
import ramana.example.niotcpserver.handler.ChannelHandler;
import ramana.example.niotcpserver.handler.impl.LoggingHandler;
import ramana.example.niotcpserver.internal.handler.ContextFactory;
import ramana.example.niotcpserver.internal.handler.InternalChannelHandler;
import ramana.example.niotcpserver.io.AllocatorInternal;
import ramana.example.niotcpserver.io.DefaultAllocator;
import ramana.example.niotcpserver.io.SslMode;
import ramana.example.niotcpserver.types.LinkedList;
import ramana.example.niotcpserver.types.ScheduledTask;
import ramana.example.niotcpserver.types.SocketOptionException;
import ramana.example.niotcpserver.util.CompletionSignal;
import ramana.example.niotcpserver.util.Constants;
import ramana.example.niotcpserver.util.SslContextUtil;
import ramana.example.niotcpserver.util.Util;
import ramana.example.niotcpserver.internal.handler.IdleTimeoutInternalChannelHandler;

import java.io.IOError;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.logging.Level;

public class Worker extends AbstractWorker {
    private static final SelectionStrategy SELECT_NOW = new SelectionStrategy(Selection.SELECT_NOW);
    private static final SelectionStrategy SELECT_TIMEOUT = new SelectionStrategy(Selection.SELECT_TIMEOUT);
    private final Queue<SocketChannel> channels = new ConcurrentLinkedQueue<>();
    private final boolean sslEnabled;
    private PriorityQueue<ScheduledTask> scheduledTasks;
    private final boolean defaultRead;
    private final Server server;
    private final List<Class<? extends ChannelHandler>> channelHandlers;
    public final AllocatorInternal<ByteBuffer> allocator;
    public AllocatorInternal<ByteBuffer> directAllocator;
    private final int idleTimeout;

    public Worker(CompletionSignal initSignal, Configuration.WorkerConfiguration configuration, Server server) {
        super(initSignal, configuration.getSelectorProvider(), configuration.getSocketOptions(), configuration.isLoggingEnabled());
        channelHandlers = configuration.getChannelHandlers();
        idleTimeout = configuration.getIdleTimeout();
        if(idleTimeout != 0) scheduledTasks = new PriorityQueue<>(Constants.SCHEDULED_TASK_QUEUE_INITIAL_CAPACITY);
        defaultRead = configuration.isDefaultRead();
        sslEnabled = configuration.isSslEnabled();
        this.server = server;
        Function<Integer, ByteBuffer> allocatorFunction = sslEnabled ? ByteBuffer::allocate : ByteBuffer::allocateDirect;
        allocator = new DefaultAllocator(allocatorFunction);
        if(sslEnabled) {
            directAllocator = new DefaultAllocator(ByteBuffer::allocateDirect);
        }
    }

    public void enqueue(SocketChannel channel) {
        try {
            channels.offer(channel);
        } catch (OutOfMemoryError error) {
            close(channel);
            return;
        }
        selector.wakeup();
    }

    @Override
    public void run() {
        try {
            selector = provider.openSelector();
            initSignal.complete();
        } catch (OutOfMemoryError | IOError | IOException | RuntimeException exception) {
            logger.log(Level.WARNING, exception.getMessage(), exception);
            cleanUp();
            initSignal.fail();
            return;
        }

        try {
            while(doWork) {
                setSelectStrategy();
                selectLoop();
                executeScheduledTasks();
                dequeue();
            }
        } finally {
            cleanUp();
        }
        logger.info(Thread.currentThread().getName() + " exiting.");
    }

    private void setSelectStrategy() {
        if(!channels.isEmpty()) {
            selectionStrategy = SELECT_NOW;
            return;
        }
        if(scheduledTasks == null || scheduledTasks.isEmpty()) {
            selectionStrategy = SELECT;
            return;
        }
        long diff = scheduledTasks.peek().scheduledTime - System.currentTimeMillis();
        if(diff > 0) {
            selectionStrategy = SELECT_TIMEOUT;
            selectionStrategy.timeout = diff;
            return;
        }
        selectionStrategy = SELECT_NOW;
    }

    private void executeScheduledTasks() {
        if(scheduledTasks == null || scheduledTasks.isEmpty()) return;
        allocator.recycle(); // free memory
        if(directAllocator != null) directAllocator.recycle();
        long currentTime = System.currentTimeMillis();
        ScheduledTask task;
        while ((task = scheduledTasks.peek()) != null) {
            if(task.scheduledTime > currentTime) return;
            task.execute();
            scheduledTasks.poll();
        }
    }

    private void cleanUp() {
        synchronized (shutDownLock) {
            close(selector);
            shutDownComplete = true;
            if(shutDownSignal != null) shutDownSignal.complete();
        }
    }

    private void dequeue() {
        SocketChannel channel;
        while((channel = channels.poll()) != null) {
            allocator.recycle(); // free memory
            if(directAllocator != null) directAllocator.recycle();
            register(channel);
        }
    }

    public void register(SocketChannel channel) {
        int ops = defaultRead ? SelectionKey.OP_READ : Constants.SELECTION_KEY_OP_NONE;
        try {
            channel.configureBlocking(false);
            setSocketOptions(channel);
            LinkedList<ChannelHandler> channelHandlerList = Util.createLinkedList(channelHandlers);
            if(loggingEnabled) channelHandlerList.addFirst(LoggingHandler.defaultInstance);
            ContextFactory factory = loggingEnabled ? ContextFactory.loggingFactory() : ContextFactory.factory();
            InternalChannelHandler internalChannelHandler = idleTimeout == 0 ? new InternalChannelHandler(channelHandlerList, allocator, factory, defaultRead) : new IdleTimeoutInternalChannelHandler(channelHandlerList, factory, defaultRead, this, idleTimeout, loggingEnabled);
            SelectionKey sk = channel.register(selector, ops, internalChannelHandler);
            internalChannelHandler.onConnect(sk, sslEnabled ? SslMode.SERVER : SslMode.NONE, directAllocator);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException
                 | InvocationTargetException | SocketOptionException | SslContextUtil.SSLContextException exception) {
            logger.log(Level.SEVERE, exception.getMessage(), exception);
            server.shutDown(true);
            throw new RuntimeException(exception); // to exit run loop
        } catch (OutOfMemoryError | IOError | IOException | RuntimeException exception) {
            logger.log(Level.WARNING, exception.getMessage(), exception);
            close(channel);
        }
    }

    @Override
    protected void handleEvent(SelectionKey sk) {
        allocator.recycle(); // free memory
        if(directAllocator != null) directAllocator.recycle();
        InternalChannelHandler internalChannelHandler = (InternalChannelHandler) sk.attachment();
        if(sk.isReadable()) internalChannelHandler.handleRead();
        if(sk.isValid() && sk.isWritable()) internalChannelHandler.handleWrite();
    }

    public void schedule(Runnable task, long futureTime) {
        scheduledTasks.offer(new ScheduledTask(task, futureTime));
    }
}
