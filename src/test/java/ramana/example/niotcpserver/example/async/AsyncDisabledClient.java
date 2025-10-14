package ramana.example.niotcpserver.example.async;

import ramana.example.niotcpserver.Client;
import ramana.example.niotcpserver.log.LogFactory;
import ramana.example.niotcpserver.types.Pair;

import java.net.SocketOption;
import java.util.logging.Logger;

import static ramana.example.niotcpserver.example.async.AsyncServer.HELLO_WORLD;

public class AsyncDisabledClient {
    private static final Logger logger = LogFactory.getLogger();

    public static void main(String[] args) {
        Pair<SocketOption, Object>[] socketOptions = new Pair[]{};
        AsyncClient.ChannelHandler handler = new AsyncClient.ChannelHandler();
        Client client = new Client().connect("localhost", 8080)
                .enableDefaultRead()
                .enableLogging()
                .channelHandler(handler);

        handler.setInput(HELLO_WORLD);
        client.run();
    }
}
