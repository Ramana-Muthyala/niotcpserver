package ramana.example.niotcpserver.internal.worker;

import ramana.example.niotcpserver.conf.Configuration;
import ramana.example.niotcpserver.types.SocketOptionException;
import ramana.example.niotcpserver.util.CompletionSignal;
import ramana.example.niotcpserver.util.Constants;

import java.io.IOError;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;

public class Acceptor extends AbstractWorker {
    private final int port;
    private final Dispatcher dispatcher;
    private final int backlog;
    private ServerSocketChannel serverSocketChannel;
    private static final int minBacklog = Constants.MIN_BACKLOG;

    public Acceptor(CompletionSignal initSignal, Configuration.ServerConfiguration configuration) {
        super(initSignal, configuration.getSelectorProvider(), configuration.getSocketOptions(), configuration.isLoggingEnabled());
        port = configuration.getPort();
        dispatcher = configuration.getDispatcher();
        backlog = configuration.getBacklog();
    }

    private void registerServerSocketChannel() throws IOException, SocketOptionException {
        serverSocketChannel = provider.openServerSocketChannel();
        serverSocketChannel.configureBlocking(false);
        setSocketOptions(serverSocketChannel);
        InetSocketAddress isa = new InetSocketAddress(port);
        if(backlog < minBacklog) serverSocketChannel.socket().bind(isa);
        else serverSocketChannel.socket().bind(isa, backlog);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }
    @Override
    protected void handleEvent(SelectionKey sk) {
        try {
            ServerSocketChannel ssc = (ServerSocketChannel)sk.channel();
            SocketChannel channel = ssc.accept();
            if(channel == null) return; // should never be null but just a double check.
            dispatcher.dispatch(channel);
        } catch (OutOfMemoryError | IOError | IOException | RuntimeException exception) {
            logger.log(Level.WARNING, exception.getMessage(), exception);
        }
    }

    @Override
    public void run() {
        try {
            selector = provider.openSelector();
            registerServerSocketChannel();
            initSignal.complete();
        } catch (OutOfMemoryError | IOError | IOException | SocketOptionException | RuntimeException exception) {
            logger.log(Level.WARNING, exception.getMessage(), exception);
            cleanUp();
            initSignal.fail();
            return;
        }

        try {
            while(doWork) {
                selectLoop();
            }
        } finally {
            cleanUp();
        }
        logger.info(Thread.currentThread().getName() + " exiting.");
    }

    private void cleanUp() {
        synchronized (shutDownLock) {
            close(selector);
            close(serverSocketChannel);
            shutDownComplete = true;
            if(shutDownSignal != null) shutDownSignal.complete();
        }
    }
}
