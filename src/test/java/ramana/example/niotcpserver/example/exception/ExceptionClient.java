package ramana.example.niotcpserver.example.exception;

import ramana.example.niotcpserver.Client;
import ramana.example.niotcpserver.handler.Context;
import ramana.example.niotcpserver.handler.impl.ChannelHandlerAdapter;
import ramana.example.niotcpserver.log.LogFactory;
import ramana.example.niotcpserver.types.InternalException;
import ramana.example.niotcpserver.util.Util;

import java.util.logging.Logger;

public class ExceptionClient {
    private static final Logger logger = LogFactory.getLogger();

    public static void main(String[] args) {
        new Client().connect("localhost", 8080)
                .enableDefaultRead()
                .enableLogging()
                .channelHandler(new ChannelHandler())
                .run();
    }

    public static class ChannelHandler extends ChannelHandlerAdapter {
        @Override
        public void onRead(Context.OnRead context, Object data) throws InternalException {
            logger.info("Received: " + Util.toString(data));
        }
    }
}
