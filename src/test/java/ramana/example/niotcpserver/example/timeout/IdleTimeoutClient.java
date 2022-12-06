package ramana.example.niotcpserver.example.timeout;

import ramana.example.niotcpserver.Client;
import ramana.example.niotcpserver.handler.Context;
import ramana.example.niotcpserver.handler.impl.ChannelHandlerAdapter;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.log.LogFactory;
import ramana.example.niotcpserver.types.InternalException;
import ramana.example.niotcpserver.types.Pair;
import ramana.example.niotcpserver.util.Util;

import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

public class IdleTimeoutClient {
    private static final Logger logger = LogFactory.getLogger();

    public static void main(String[] args) {
        Pair<SocketOption, Object>[] socketOptions = new Pair[]{};
        new Client().connect("localhost", 8080)
                .socketOptions(socketOptions)
                .enableDefaultRead()
                .enableLogging()
                .channelHandler(new ChannelHandler())
                .run();
    }

    public static class ChannelHandler extends ChannelHandlerAdapter {
        @Override
        public void onConnect(Context.OnConnect context, Object data) throws InternalException {
            Allocator.Resource<ByteBuffer> resource = context.allocator().allocate(1024);
            ByteBuffer buffer = resource.get();
            String greeting = "Hello World !!";
            buffer.put(greeting.getBytes());
            context.write(resource);
            context.flush();
        }

        @Override
        public void onRead(Context.OnRead context, Object data) throws InternalException {
            logger.info("Received: " + Util.toString(data));
        }

        @Override
        public void onReadComplete(Context.OnRead context, Object data) throws InternalException {
            context.close();
        }
    }
}
