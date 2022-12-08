package ramana.example.niotcpserver.example.latencytest;

import ramana.example.niotcpserver.Client;
import ramana.example.niotcpserver.handler.Context;
import ramana.example.niotcpserver.handler.impl.ChannelHandlerAdapter;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.log.LogFactory;
import ramana.example.niotcpserver.types.InternalException;
import ramana.example.niotcpserver.util.Util;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

public class LatencyTestClientTwo {
    private static final Logger logger = LogFactory.getLogger();

    public static void main(String[] args) {
        new Client().connect("localhost", 8007)
                .enableDefaultRead()
                //.enableLogging()
                .channelHandler(new ChannelHandler())
                .run();
    }

    public static class ChannelHandler extends ChannelHandlerAdapter {
        private int counter;
        private long start;
        @Override
        public void onConnect(Context.OnConnect context, Object data) throws InternalException {
            start = System.nanoTime();
            write0(context);
        }

        @Override
        public void onRead(Context.OnRead context, Object data) throws InternalException {
            if(!Util.toString(data).equals("Hello World !!")) {
                logger.info("Received: " + Util.toString(data));
            }
            counter++;
            if(counter == 100000) {
                context.close();
                logger.info("Time taken: (ms): " + ((double)(System.nanoTime() - start) / 1000000));
                return;
            }
            write0(context);
        }

        private void write0(Context.OnRead context) throws InternalException {
            Allocator.Resource<ByteBuffer> resource = context.allocator().allocate(1024);
            ByteBuffer buffer = resource.get();
            String greeting = "Hello World !!";
            buffer.put(greeting.getBytes());
            context.write(resource);
            context.flush();
        }
    }
}
