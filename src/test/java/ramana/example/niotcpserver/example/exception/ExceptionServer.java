package ramana.example.niotcpserver.example.exception;

import ramana.example.niotcpserver.Bootstrap;
import ramana.example.niotcpserver.handler.Context;
import ramana.example.niotcpserver.handler.impl.ChannelHandlerAdapter;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.log.LogFactory;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExceptionServer {
    public static void main(String[] args) {
        new Bootstrap().listen(8080)
                .backlog(100)
                .numOfWorkers(4)
                .enableDefaultRead()
                .enableLogging()
                .channelHandler(ChannelHandler.class)
                .start();
    }

    public static class ChannelHandler extends ChannelHandlerAdapter {
        private static final Logger logger = LogFactory.getLogger();
        @Override
        public void onConnect(Context.OnConnect context, Object data) throws InternalException {
            Allocator.Resource<ByteBuffer> resource = context.allocator().allocate(1024);
            ByteBuffer buffer = resource.get();
            String greeting = "Hello World !!";
            buffer.put(greeting.getBytes());
            context.write(resource);      // This should be written to the client.

            // any exception during processing will result in closing the connection and
            // calling onClose
            // data will be flushed before closing the connection.
            throw new RuntimeException("Simulating an exception while processing");
        }

        @Override
        public void onClose(Context.OnClose context, Object data, Throwable cause) throws InternalException {
            logger.log(Level.WARNING, "I just saw an exception", cause);
            context.next(data, cause);
        }
    }
}
