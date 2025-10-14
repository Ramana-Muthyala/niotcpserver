package ramana.example.niotcpserver.example.async;

import ramana.example.niotcpserver.Bootstrap;
import ramana.example.niotcpserver.types.Pair;

import java.net.SocketOption;

public class AsyncDisabledServer {
    public static void main(String[] args) {
        Pair<SocketOption, Object>[] serverSocketOptions = new Pair[]{};
        Pair<SocketOption, Object>[] socketOptions = new Pair[]{};
        new Bootstrap().listen(8080)
                .backlog(100)
                .serverSocketOptions(serverSocketOptions)
                .numOfWorkers(4)
                .socketOptions(socketOptions)
                .enableDefaultRead()
                .enableLogging()
                .channelHandler(AsyncServer.ChannelHandlerOne.class)
                .channelHandler(AsyncServer.ChannelHandlerTwo.class)
                .start();
    }
}
