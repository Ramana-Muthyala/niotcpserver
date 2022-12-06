package ramana.example.niotcpserver.worker;

import ramana.example.niotcpserver.Client;
import ramana.example.niotcpserver.handler.ChannelHandler;
import ramana.example.niotcpserver.handler.impl.LoggingHandler;
import ramana.example.niotcpserver.io.ClientAllocator;
import ramana.example.niotcpserver.types.LinkedList;
import ramana.example.niotcpserver.types.SocketOptionException;
import ramana.example.niotcpserver.util.CompletionSignal;
import ramana.example.niotcpserver.util.Util;
import ramana.example.niotcpserver.worker.impl.ContextFactory;
import ramana.example.niotcpserver.worker.impl.InternalChannelHandler;

import java.io.IOError;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.logging.Level;

public class ClientWorker extends AbstractWorker {
    private final Client client;
    private SocketChannel channel;
    private final ClientAllocator allocator = new ClientAllocator();

    public ClientWorker(Client client, CompletionSignal initSignal, SelectorProvider provider) {
        super(initSignal, provider, client.getSocketOptions(), client.isLoggingEnabled());
        this.client = client;
    }

    @Override
    protected void handleEvent(SelectionKey sk) {
        InternalChannelHandler internalChannelHandler = (InternalChannelHandler) sk.attachment();
        try {
            if(sk.isConnectable()) {
                channel.finishConnect();
                sk.interestOps(sk.interestOps() & (~SelectionKey.OP_CONNECT));
                internalChannelHandler.onConnect(sk);
                if(!channel.isOpen()) doWork = false;
                return;
            }
            allocator.recycle(); // free memory
            if(sk.isReadable()) internalChannelHandler.handleRead();
            if(sk.isValid() && sk.isWritable()) internalChannelHandler.handleWrite();
        } catch (OutOfMemoryError | IOError | IOException | RuntimeException exception) {
            doWork = false;
            logger.log(Level.WARNING, exception.getMessage(), exception);
            close(channel);
        }
        if(!channel.isOpen()) doWork = false;
    }

    @Override
    public void run() {
        try {
            selector = provider.openSelector();
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            setSocketOptions(channel);
            int ops = client.isDefaultRead() ? SelectionKey.OP_CONNECT | SelectionKey.OP_READ : SelectionKey.OP_CONNECT;
            LinkedList<ChannelHandler> channelHandlerList = Util.createLinkedListFromInstanceList(client.getChannelHandlers());
            if(loggingEnabled) channelHandlerList.addFirst(LoggingHandler.defaultInstance);
            ContextFactory factory = loggingEnabled ? ContextFactory.loggingFactory() : ContextFactory.factory();
            InternalChannelHandler internalChannelHandler = new InternalChannelHandler(channelHandlerList, allocator, factory);
            channel.register(selector, ops, internalChannelHandler);
            channel.connect(new InetSocketAddress(client.getHost(), client.getPort()));
        } catch (OutOfMemoryError | IOError | IOException | SocketOptionException | RuntimeException exception) {
            logger.log(Level.WARNING, exception.getMessage(), exception);
            cleanUp();
            if(initSignal != null) initSignal.fail();
            return;
        }

        try {
            while(doWork) {
                selectLoop();
            }
        } finally {
            cleanUp();
            if(initSignal != null) initSignal.complete();
        }
        logger.info(Thread.currentThread().getName() + " exiting.");
    }

    private void cleanUp() {
        close(channel);
        close(selector);
    }
}
