package ramana.example.niotcpserver.example.simpleprotocol;

import ramana.example.niotcpserver.Bootstrap;
import ramana.example.niotcpserver.handler.Context;
import ramana.example.niotcpserver.handler.impl.ChannelHandlerAdapter;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.log.LogFactory;
import ramana.example.niotcpserver.types.InternalException;
import ramana.example.niotcpserver.util.Util;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

public class SimpleProtocolServer {
    private static final Logger logger = LogFactory.getLogger();

    public static void main(String[] args) {
        new Bootstrap().listen(8080)
                .enableDefaultRead()
                .enableLogging()
                .channelHandler(ChannelHandler.class)
                .start();
    }

    public static class ChannelHandler extends ChannelHandlerAdapter {
        private int counter;

        @Override
        public void onRead(Context.OnRead context, Object data) throws InternalException {
            logger.info("Received: " + Util.toString(data) + ", counter: " + counter);
            write0(context);
            counter++;
            if(counter == 2) {
                context.close();    // contract: server closes after reading and writing twice
            }
        }

        private void write0(Context.OnRead context) throws InternalException {
            Allocator.Resource<ByteBuffer> resource = context.allocator().allocate(1024);
            ByteBuffer buffer = resource.get();
            String greeting = "Hello from server !!";
            buffer.put(greeting.getBytes());
            context.write(resource);
            context.flush();
        }
    }
}
