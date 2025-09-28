package ramana.example.niotcpserver.example.latencytest;

import ramana.example.niotcpserver.Client;
import ramana.example.niotcpserver.handler.Context;
import ramana.example.niotcpserver.handler.impl.ChannelHandlerAdapter;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.log.LogFactory;
import ramana.example.niotcpserver.types.InternalException;
import ramana.example.niotcpserver.util.Util;
import ramana.example.niotcpserver.internal.worker.ClientWorker;

import java.nio.ByteBuffer;
import java.nio.channels.spi.SelectorProvider;
import java.util.logging.Logger;

public class LatencyTestClientOne {
    private static final Logger logger = LogFactory.getLogger();

    public static void main(String[] args) {
        new TestClient().connect("localhost", 8007)
                .enableDefaultRead()
                //.enableLogging()
                .channelHandler(new ChannelHandler())
                .run();
    }

    private static class TestClient extends Client {
        private final SelectorProvider provider = SelectorProvider.provider();
        @Override
        public void run() {
            int loop = 1000;
            long start = System.nanoTime();
            for (int i = 0; i < loop; i++) {
                new ClientWorker(this, null, null, provider).run();
            }
            long end = System.nanoTime() - start;
            logger.info("Time taken (ms): " + ((double)end / 1000000));

            start = System.nanoTime();
            for (int i = 0; i < loop; i++) {
                new ClientWorker(this, null, null, provider).run();
            }
            end = System.nanoTime() - start;
            logger.info("Time taken (ms): " + ((double)end / 1000000));

            start = System.nanoTime();
            for (int i = 0; i < loop; i++) {
                new ClientWorker(this, null, null, provider).run();
            }
            end = System.nanoTime() - start;
            logger.info("Time taken (ms): " + ((double)end / 1000000));

            start = System.nanoTime();
            for (int i = 0; i < loop; i++) {
                new ClientWorker(this, null, null, provider).run();
            }
            end = System.nanoTime() - start;
            logger.info("Time taken (ms): " + ((double)end / 1000000));
        }
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
            if(Util.toString(data).equals("Hello World !!")) return;
            logger.info("Received: " + Util.toString(data));
        }
    }
}
