package ramana.example.niotcpserver.example.discard;

import ramana.example.niotcpserver.Bootstrap;
import ramana.example.niotcpserver.handler.Context;
import ramana.example.niotcpserver.handler.impl.ChannelHandlerAdapter;
import ramana.example.niotcpserver.types.InternalException;

public class DiscardServer {

    public static void main(String[] args) {
        new Bootstrap().listen(8080)
                .enableDefaultRead()
                .enableLogging()
                .channelHandler(ChannelHandler.class)
                .start();
    }

    public static class ChannelHandler extends ChannelHandlerAdapter {
        @Override
        public void onReadComplete(Context.OnRead context, Object data) throws InternalException {
            context.close();
        }
    }
}
