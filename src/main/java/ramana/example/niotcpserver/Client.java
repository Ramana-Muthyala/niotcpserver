package ramana.example.niotcpserver;

import ramana.example.niotcpserver.handler.ChannelHandler;
import ramana.example.niotcpserver.types.Pair;
import ramana.example.niotcpserver.util.CompletionSignal;
import ramana.example.niotcpserver.internal.worker.ClientWorker;

import java.net.SocketOption;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.List;

public class Client {
    private String host;
    private int port;
    private Pair<SocketOption, Object>[] socketOptions;
    private boolean defaultRead;
    private final List<ChannelHandler> channelHandlers = new ArrayList<>();
    private boolean loggingEnabled;
    private boolean sslEnabled;
    private boolean sslEnabledWithDefaultTrustManager;

    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    public Client connect(String host, int port) {
        this.host = host;
        this.port = port;
        return this;
    }

    public Client socketOptions(Pair<SocketOption, Object>[] socketOptions) {
        this.socketOptions = socketOptions;
        return this;
    }

    public Client enableDefaultRead() {
        defaultRead = true;
        return this;
    }

    public Client channelHandler(ChannelHandler channelHandler) {
        channelHandlers.add(channelHandler);
        return this;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public boolean isDefaultRead() {
        return defaultRead;
    }

    public List<ChannelHandler> getChannelHandlers() {
        return channelHandlers;
    }

    public Pair<SocketOption, Object>[] getSocketOptions() {
        return socketOptions;
    }

    private void validate() {
        StringBuilder builder = new StringBuilder();
        if(host == null) builder.append("Host not set.\n");
        if(port == 0) builder.append("Port not set.\n");
        if(channelHandlers.size() == 0) builder.append("ChannelHandler not set.\n");
        String errors = builder.toString();
        if(!errors.isEmpty()) throw new RuntimeException(errors);
    }

    public void run() {
        validate();
        SelectorProvider provider = SelectorProvider.provider();
        try {
            CompletionSignal initSignal = new CompletionSignal(1);
            new ClientWorker(this, initSignal, null, provider).run();
            initSignal.doWait();
        } catch (CompletionSignal.CompletionSignalException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        validate();
        SelectorProvider provider = SelectorProvider.provider();
        CompletionSignal initSignal;
        try {
            initSignal = new CompletionSignal(1);
            new Thread(new ClientWorker(this, initSignal, null, provider)).start();
            initSignal.doWait();
        } catch (CompletionSignal.CompletionSignalException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void startAndWaitForOnConnectSignal() {
        validate();
        SelectorProvider provider = SelectorProvider.provider();
        CompletionSignal onConnectSignal;
        try {
            onConnectSignal = new CompletionSignal(1);
            new Thread(new ClientWorker(this, null, onConnectSignal, provider)).start();
            onConnectSignal.doWait();
        } catch (CompletionSignal.CompletionSignalException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Client enableLogging() {
        loggingEnabled = true;
        return this;
    }

    public Client enableSsl() {
        sslEnabled = true;
        return this;
    }

    public boolean isSslEnabled() {
        return sslEnabled;
    }

    public Client enableSslWithDefaultTrustManager() {
        sslEnabledWithDefaultTrustManager = true;
        return this;
    }

    public boolean isSslEnabledWithDefaultTrustManager() {
        return sslEnabledWithDefaultTrustManager;
    }
}
