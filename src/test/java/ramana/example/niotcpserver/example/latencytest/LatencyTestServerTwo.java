package ramana.example.niotcpserver.example.latencytest;

import ramana.example.niotcpserver.Bootstrap;
import ramana.example.niotcpserver.handler.Context;
import ramana.example.niotcpserver.handler.impl.ChannelHandlerAdapter;
import ramana.example.niotcpserver.types.InternalException;

public class LatencyTestServerTwo {

    public static void main(String[] args) {
        new Bootstrap().listen(8007)
                .enableDefaultRead()
                .numOfWorkers(4)
                .backlog(100)
                .channelHandler(ChannelHandler.class)
                .start();
    }

    public static class ChannelHandler extends ChannelHandlerAdapter {
        @Override
        public void onRead(Context.OnRead context, Object data) throws InternalException {
            context.write(data);
            context.flush();
        }
    }
}
