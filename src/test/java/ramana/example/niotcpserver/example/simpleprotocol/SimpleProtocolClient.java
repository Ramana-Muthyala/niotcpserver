package ramana.example.niotcpserver.example.simpleprotocol;

import ramana.example.niotcpserver.Client;
import ramana.example.niotcpserver.handler.Context;
import ramana.example.niotcpserver.handler.impl.ChannelHandlerAdapter;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.log.LogFactory;
import ramana.example.niotcpserver.types.InternalException;
import ramana.example.niotcpserver.util.Util;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

public class SimpleProtocolClient {
    private static final Logger logger = LogFactory.getLogger();

    public static void main(String[] args) {
        new Client().connect("localhost", 8080)
                .enableDefaultRead()
                .enableLogging()
                .channelHandler(new ChannelHandler())
                .run();
    }

    public static class ChannelHandler extends ChannelHandlerAdapter {
        private int counter;

        @Override
        public void onConnect(Context.OnConnect context, Object data) throws InternalException {
            write0(context);
            counter++;
        }

        @Override
        public void onRead(Context.OnRead context, Object data) throws InternalException {
            logger.info("Received: " + Util.toString(data) + ", counter: " + counter);
            if(counter == 2) {
                return; // contract: do not write to server after reading twice. wait for server to close.
            }
            write0(context);
            counter++;
            context.flush();
        }

        @Override
        public void onReadComplete(Context.OnRead context, Object data) throws InternalException {
            context.close();
        }

        private void write0(Context.OnRead context) throws InternalException {
            Allocator.Resource<ByteBuffer> resource = context.allocator().allocate(1024);
            ByteBuffer buffer = resource.get();
            String greeting = "Hello from client !!";
            buffer.put(greeting.getBytes());
            context.write(resource);
            context.flush();
        }
    }
}
