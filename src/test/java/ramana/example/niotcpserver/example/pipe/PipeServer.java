package ramana.example.niotcpserver.example.pipe;

import ramana.example.niotcpserver.Bootstrap;
import ramana.example.niotcpserver.Client;
import ramana.example.niotcpserver.handler.Context;
import ramana.example.niotcpserver.handler.impl.ChannelHandlerAdapter;
import ramana.example.niotcpserver.types.InternalException;

public class PipeServer {
    public static void main(String[] args) {
        new Bootstrap().listen(8080)
                .enableDefaultRead()
                .enableLogging()
                .channelHandler(ChannelHandler.class)
                .start();
    }

    public static class ChannelHandler extends ChannelHandlerAdapter {
        @Override
        public void onRead(Context.OnRead context, Object data) throws InternalException {
            ClientChannelHandler channelHandler = new ClientChannelHandler(context, data);
            new Client().connect("localhost", 8081)
                    .enableDefaultRead()
                    .enableLogging()
                    .channelHandler(channelHandler)
                    .run();
        }
    }

    public static class ClientChannelHandler extends ChannelHandlerAdapter {
        private final Context.OnRead onReadContext;
        private final Object onReadData;

        public ClientChannelHandler(Context.OnRead context, Object data) {
            this.onReadContext = context;
            this.onReadData = data;
        }

        @Override
        public void onConnect(Context.OnConnect context, Object data) throws InternalException {
            context.write(onReadData);
            context.flush();
        }

        @Override
        public void onRead(Context.OnRead context, Object data) throws InternalException {
            onReadContext.write(data);
            onReadContext.close();
        }
    }
}
