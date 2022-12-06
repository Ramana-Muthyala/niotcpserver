package ramana.example.niotcpserver;

import ramana.example.niotcpserver.handler.ChannelHandler;
import ramana.example.niotcpserver.log.LogFactory;
import ramana.example.niotcpserver.types.Pair;
import ramana.example.niotcpserver.util.CompletionSignal;
import ramana.example.niotcpserver.util.Constants;
import ramana.example.niotcpserver.util.Util;

import java.net.SocketOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Bootstrap {
    private int port;
    private final List<Class<? extends ChannelHandler>> channelHandlers = new ArrayList<>();
    private static final Logger logger = LogFactory.getLogger();
    private Pair<SocketOption, Object>[] serverSocketOptions;
    private Pair<SocketOption, Object>[] socketOptions;
    private int idleTimeout;
    private boolean defaultRead;
    private int backlog;
    private boolean loggingEnabled;

    public int getBacklog() {
        return backlog;
    }

    public boolean isDefaultRead() {
        return defaultRead;
    }

    public int getIdleTimeout() {
        return idleTimeout;
    }

    public Pair<SocketOption, Object>[] getServerSocketOptions() {
        return serverSocketOptions;
    }

    public Pair<SocketOption, Object>[] getSocketOptions() {
        return socketOptions;
    }

    public int getPort() {
        return port;
    }

    public int getNumWorkers() {
        return numWorkers;
    }

    public List<Class<? extends ChannelHandler>> getChannelHandlers() {
        return channelHandlers;
    }

    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    private int numWorkers = Constants.DEFAULT_NUM_WORKERS;

    public Bootstrap channelHandler(Class<? extends ChannelHandler> channelHandler) {
        channelHandlers.add(channelHandler);
        return this;
    }

    public void start() {
        long beginTime = System.currentTimeMillis();
        validate();
        logger.info("NioTcpServer: Workers: " + numWorkers);
        logger.info("Channel Handlers: " + Util.normalizeClassName(channelHandlers));
        Server server = new Server(this);
        try {
            server.start();
            logger.info("Listening on port: " + port + ", Time taken: " + ((double)(System.currentTimeMillis() - beginTime) / 1000) + " sec");
        } catch (CompletionSignal.CompletionSignalException | InterruptedException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            server.shutDown(false);
        }
    }

    private void validate() {
        StringBuilder builder = new StringBuilder();
        if(port == 0) builder.append("Port not set.\n");
        if(channelHandlers.size() == 0) builder.append("ChannelHandler not set.\n");
        if(idleTimeout < Constants.MIN_IDLE_TIMEOUT) builder.append("Idle timeout cannot be < " + Constants.MIN_IDLE_TIMEOUT + ".\n");
        String errors = builder.toString();
        if(!errors.isEmpty()) throw new RuntimeException(errors);
    }

    public Bootstrap numOfWorkers(int numWorkers) {
        this.numWorkers = numWorkers;
        return this;
    }

    public Bootstrap listen(int port) {
        this.port = port;
        return this;
    }

    public Bootstrap serverSocketOptions(Pair<SocketOption, Object>[] serverSocketOptions) {
        this.serverSocketOptions = serverSocketOptions;
        return this;
    }

    public Bootstrap socketOptions(Pair<SocketOption, Object>[] socketOptions) {
        this.socketOptions = socketOptions;
        return this;
    }

    public Bootstrap idleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
        return this;
    }

    public Bootstrap enableDefaultRead() {
        defaultRead = true;
        return this;
    }

    public Bootstrap backlog(int backlog) {
        this.backlog = backlog;
        return this;
    }

    public Bootstrap enableLogging() {
        loggingEnabled = true;
        return this;
    }
}
