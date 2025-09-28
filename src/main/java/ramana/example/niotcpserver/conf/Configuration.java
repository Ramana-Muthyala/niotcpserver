package ramana.example.niotcpserver.conf;

import ramana.example.niotcpserver.handler.ChannelHandler;
import ramana.example.niotcpserver.types.Pair;
import ramana.example.niotcpserver.internal.worker.Dispatcher;

import java.net.SocketOption;
import java.nio.channels.spi.SelectorProvider;
import java.util.List;

public class Configuration {
    private final Pair<SocketOption, Object>[] socketOptions;
    private final SelectorProvider selectorProvider;
    private final boolean loggingEnabled;

    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    public SelectorProvider getSelectorProvider() {
        return selectorProvider;
    }
    public Pair<SocketOption, Object>[] getSocketOptions() {
        return socketOptions;
    }

    public Configuration(SelectorProvider selectorProvider, Pair<SocketOption, Object>[] socketOptions, boolean loggingEnabled) {
        this.selectorProvider = selectorProvider;
        this.socketOptions = socketOptions;
        this.loggingEnabled = loggingEnabled;
    }

    public static class ServerConfiguration extends Configuration {
        private final int port;
        private final Dispatcher dispatcher;
        private final int backlog;

        public int getBacklog() {
            return backlog;
        }

        public int getPort() {
            return port;
        }
        public Dispatcher getDispatcher() {
            return dispatcher;
        }

        public ServerConfiguration(SelectorProvider selectorProvider, Pair<SocketOption, Object>[] socketOptions, int port, Dispatcher dispatcher, int backlog) {
            super(selectorProvider, socketOptions, false);
            this.port = port;
            this.dispatcher = dispatcher;
            this.backlog = backlog;
        }
    }

    public static class WorkerConfiguration extends Configuration {
        private final int idleTimeout;
        private final boolean defaultRead;
        private final boolean sslEnabled;

        public List<Class<? extends ChannelHandler>> getChannelHandlers() {
            return channelHandlers;
        }

        private final List<Class<? extends ChannelHandler>> channelHandlers;

        public boolean isDefaultRead() {
            return defaultRead;
        }
        public int getIdleTimeout() {
            return idleTimeout;
        }

        public boolean isSslEnabled() {
            return sslEnabled;
        }

        public WorkerConfiguration(SelectorProvider selectorProvider, Pair<SocketOption, Object>[] socketOptions, List<Class<? extends ChannelHandler>> channelHandlers, int idleTimeout, boolean defaultRead, boolean loggingEnabled, boolean sslEnabled) {
            super(selectorProvider, socketOptions, loggingEnabled);
            this.channelHandlers = channelHandlers;
            this.idleTimeout = idleTimeout;
            this.defaultRead = defaultRead;
            this.sslEnabled = sslEnabled;
        }
    }
}
