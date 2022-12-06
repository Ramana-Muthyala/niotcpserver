package ramana.example.niotcpserver.example.discard;

import ramana.example.niotcpserver.Bootstrap;
import ramana.example.niotcpserver.handler.impl.ChannelHandlerAdapter;

public class DiscardServer {

    public static void main(String[] args) {
        new Bootstrap().listen(8080)
                .enableDefaultRead()
                .enableLogging()
                .channelHandler(ChannelHandler.class)
                .start();
    }

    public static class ChannelHandler extends ChannelHandlerAdapter {
        // No overriding of any methods required
    }
}
