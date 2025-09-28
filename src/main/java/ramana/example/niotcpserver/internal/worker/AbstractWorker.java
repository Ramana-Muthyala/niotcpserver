package ramana.example.niotcpserver.internal.worker;

import ramana.example.niotcpserver.log.LogFactory;
import ramana.example.niotcpserver.types.Pair;
import ramana.example.niotcpserver.types.SocketOptionException;
import ramana.example.niotcpserver.util.CompletionSignal;

import java.io.Closeable;
import java.io.IOError;
import java.io.IOException;
import java.net.SocketOption;
import java.nio.channels.NetworkChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractWorker implements Runnable {
    protected static final Logger logger = LogFactory.getLogger();
    protected static final SelectionStrategy SELECT = new SelectionStrategy(Selection.SELECT);
    protected final CompletionSignal initSignal;
    protected final SelectorProvider provider;
    protected final Pair<SocketOption, Object>[] socketOptions;
    protected final boolean loggingEnabled;
    protected Selector selector;
    protected CompletionSignal shutDownSignal;
    protected final Object shutDownLock = new Object();
    protected boolean shutDownComplete;
    protected SelectionStrategy selectionStrategy = SELECT;

    protected AbstractWorker(CompletionSignal initSignal, SelectorProvider provider, Pair<SocketOption, Object>[] socketOptions, boolean loggingEnabled) {
        this.initSignal = initSignal;
        this.provider = provider;
        this.socketOptions = socketOptions;
        this.loggingEnabled = loggingEnabled;

    }

    protected void setSocketOptions(NetworkChannel channel) throws SocketOptionException {
        if(socketOptions == null) return;
        try {
            for (Pair<SocketOption, Object> pair: socketOptions) {
                channel.setOption(pair.key, pair.value);
            }
        } catch (IOException | UnsupportedOperationException | IllegalArgumentException exception) {
            throw new SocketOptionException(exception);
        }
    }

    protected volatile boolean doWork = true;
    public void shutDown(CompletionSignal shutDownSignal) {
        synchronized (shutDownLock) {
            if(shutDownComplete) {
                shutDownSignal.complete();
                return;
            }
            doWork = false;
            this.shutDownSignal = shutDownSignal;
            selector.wakeup();
        }
    }

    protected enum Selection {SELECT, SELECT_NOW, SELECT_TIMEOUT}

    protected static class SelectionStrategy {
        public long timeout;
        public Selection selection;

        public SelectionStrategy(Selection selection) {
            this.selection = selection;
        }
    }

    protected void selectLoop() {
        try {
            SelectionStrategy strategy = selectionStrategy;
            int readyKeysCount = 0;
            switch (strategy.selection) {
                case SELECT:
                    readyKeysCount = selector.select();
                    break;
                case SELECT_NOW:
                    readyKeysCount = selector.selectNow();
                    break;
                case SELECT_TIMEOUT:
                    readyKeysCount = selector.select(strategy.timeout);
                    break;
            }
            if(readyKeysCount == 0) return;
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> i = selectedKeys.iterator();
            while (i.hasNext()) {
                SelectionKey sk = i.next();
                i.remove();
                if(sk.isValid()) handleEvent(sk);
            }
        } catch (OutOfMemoryError | IOError | IOException | RuntimeException exception) {
            logger.log(Level.WARNING, exception.getMessage(), exception);
        }
    }

    protected void close(Closeable closeable) {
        try {
            if(closeable != null) closeable.close();
        } catch (IOException e) {
            // ignore
        }
    }
    protected abstract void handleEvent(SelectionKey sk);
}
