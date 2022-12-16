package ramana.example.niotcpserver.example.codec.http;

import ramana.example.niotcpserver.Client;
import ramana.example.niotcpserver.handler.Context;
import ramana.example.niotcpserver.handler.impl.ChannelHandlerAdapter;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.log.LogFactory;
import ramana.example.niotcpserver.types.InternalException;
import ramana.example.niotcpserver.util.Util;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

public class EchoHttpClientConnectRequest {
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
        public void onConnect(Context.OnConnect context, Object data) throws InternalException {
            Allocator.Resource<ByteBuffer> resource = context.allocator().allocate(1024);
            ByteBuffer buffer = resource.get();
            String request = "CONNECT localhost:8080 HTTP/1.1\r\n" +
                    "Host:localhost\r\n" +
                    "\r\n";
            buffer.put(request.getBytes());
            context.write(resource);
            context.flush();
        }

        @Override
        public void onRead(Context.OnRead context, Object data) throws InternalException {
            logger.info("Received: \n" + Util.toString(data));
            context.close();
        }
    }
}
