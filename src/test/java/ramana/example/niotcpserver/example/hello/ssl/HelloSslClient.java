package ramana.example.niotcpserver.example.hello.ssl;

import ramana.example.niotcpserver.Client;
import ramana.example.niotcpserver.handler.Context;
import ramana.example.niotcpserver.handler.impl.ChannelHandlerAdapter;
import ramana.example.niotcpserver.log.LogFactory;
import ramana.example.niotcpserver.types.InternalException;
import ramana.example.niotcpserver.types.Pair;
import ramana.example.niotcpserver.util.Util;

import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.util.logging.Logger;

public class HelloSslClient {
    private static final Logger logger = LogFactory.getLogger();

    public static void main(String[] args) {
        Pair<SocketOption, Object>[] socketOptions = new Pair[]{
                new Pair(StandardSocketOptions.TCP_NODELAY, true)
        };
        new Client().connect("localhost", 8443)
                .enableDefaultRead()
                .enableLogging()
                .enableSsl()
                .socketOptions(socketOptions)
                .channelHandler(new ChannelHandler())
                .run();
    }

    public static class ChannelHandler extends ChannelHandlerAdapter {
        @Override
        public void onRead(Context.OnRead context, Object data) throws InternalException {
            logger.info("Received: " + Util.toString(data));
            context.write(data);
            context.flush();
            context.close();
        }
    }
}
