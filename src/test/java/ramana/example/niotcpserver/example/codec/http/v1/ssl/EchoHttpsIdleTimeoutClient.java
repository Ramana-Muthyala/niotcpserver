package ramana.example.niotcpserver.example.codec.http.v1.ssl;

import ramana.example.niotcpserver.Client;
import ramana.example.niotcpserver.handler.Context;
import ramana.example.niotcpserver.handler.impl.ChannelHandlerAdapter;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.log.LogFactory;
import ramana.example.niotcpserver.types.InternalException;
import ramana.example.niotcpserver.util.Util;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

public class EchoHttpsIdleTimeoutClient {
    private static final Logger logger = LogFactory.getLogger();

    public static void main(String[] args) {
        new Client().connect("localhost", 8443)
                .enableDefaultRead()
                .enableLogging()
                .enableSsl()
                .channelHandler(new ChannelHandler())
                .run();
    }

    public static class ChannelHandler extends ChannelHandlerAdapter {
        @Override
        public void onConnect(Context.OnConnect context, Object data) throws InternalException {
            Allocator.Resource<ByteBuffer> resource = context.allocator().allocate(1024);
            ByteBuffer buffer = resource.get();
            String request = "GET /index.html?name=ramana HTTP/1.1\r\n" +
                    "Host:localhost\r\n" +
                    "User-Agent:NIOTCPServer/1.0\r\n" +
                    "Accept:*.*\r\n" +
                    "Accept-Encoding:gzip, deflate, br\r\n" +
                    "Connection:keep-alive\r\n" +
                    "\r\n";
            buffer.put(request.getBytes());
            context.write(resource);
            context.flush();
        }

        @Override
        public void onRead(Context.OnRead context, Object data) throws InternalException {
            logger.info("Received: \n" + Util.toString(data));
        }
    }
}
