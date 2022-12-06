package ramana.example.niotcpserver.example.echo;

import ramana.example.niotcpserver.Bootstrap;
import ramana.example.niotcpserver.handler.Context;
import ramana.example.niotcpserver.handler.impl.ChannelHandlerAdapter;
import ramana.example.niotcpserver.types.InternalException;
import ramana.example.niotcpserver.types.Pair;

import java.net.SocketOption;

public class EchoServerNoDefaultRead {
    public static void main(String[] args) {
        Pair<SocketOption, Object>[] serverSocketOptions = new Pair[]{};
        Pair<SocketOption, Object>[] socketOptions = new Pair[]{};
        new Bootstrap().listen(8080)
                .backlog(100)
                .serverSocketOptions(serverSocketOptions)
                .numOfWorkers(4)
                .socketOptions(socketOptions)
                .enableLogging()
                .channelHandler(ChannelHandler.class)
                .start();
    }

    public static class ChannelHandler extends ChannelHandlerAdapter {
        @Override
        public void onConnect(Context.OnConnect context, Object data) throws InternalException {
            context.fireReadInterest();
        }

        @Override
        public void onRead(Context.OnRead context, Object data) throws InternalException {
            context.write(data);
            context.close();
        }
    }
}
