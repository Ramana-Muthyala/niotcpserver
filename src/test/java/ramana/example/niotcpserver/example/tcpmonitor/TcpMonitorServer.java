package ramana.example.niotcpserver.example.tcpmonitor;

import ramana.example.niotcpserver.Bootstrap;
import ramana.example.niotcpserver.Client;
import ramana.example.niotcpserver.handler.Context;
import ramana.example.niotcpserver.handler.impl.ChannelHandlerAdapter;
import ramana.example.niotcpserver.types.InternalException;
import ramana.example.niotcpserver.util.CompletionSignal;

public class TcpMonitorServer {

    public static void main(String[] args) {
        new Bootstrap().listen(8080)
                .enableDefaultRead()
                .enableLogging()
                .channelHandler(TcpMonitorChannelHandler.class)
                .start();
    }

    public static class TcpMonitorChannelHandler extends ChannelHandlerAdapter {
        private ClientChannelHandler clientChannelHandler;
        private Context.OnWrite context;

        @Override
        public void onConnect(Context.OnConnect context, Object data) throws InternalException {
            this.context = context;
            clientChannelHandler = new ClientChannelHandler(this);
            try {
                new Client().connect("localhost", 8081)
                        .enableDefaultRead()
                        .enableLogging()
                        .channelHandler(clientChannelHandler)
                        .startAndWaitForOnConnectSignal();
            } catch (CompletionSignal.CompletionSignalException | InterruptedException exception) {
                throw new RuntimeException(exception);
            }
        }

        @Override
        public void onRead(Context.OnRead context, Object data) throws InternalException {
            this.context = context;
            clientChannelHandler.context.write(data);
            clientChannelHandler.context.flush();
        }

        @Override
        public void onReadComplete(Context.OnRead context, Object data) throws InternalException {
            this.context = context;
            clientChannelHandler.context.close();
        }
    }

    public static class ClientChannelHandler extends ChannelHandlerAdapter {
        private final TcpMonitorChannelHandler tcpMonitorChannelHandler;
        private Context.OnWrite context;

        public ClientChannelHandler(TcpMonitorChannelHandler tcpMonitorChannelHandler) {
            this.tcpMonitorChannelHandler = tcpMonitorChannelHandler;
        }

        @Override
        public void onConnect(Context.OnConnect context, Object data) throws InternalException {
            this.context = context;
        }

        @Override
        public void onRead(Context.OnRead context, Object data) throws InternalException {
            this.context = context;
            tcpMonitorChannelHandler.context.write(data);
            tcpMonitorChannelHandler.context.flush();
        }

        @Override
        public void onReadComplete(Context.OnRead context, Object data) throws InternalException {
            this.context = context;
            tcpMonitorChannelHandler.context.close();
        }
    }
}
