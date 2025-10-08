package ramana.example.niotcpserver.example.async;

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

import static ramana.example.niotcpserver.example.async.AsyncServer.*;

public class AsyncClient {
    private static final Logger logger = LogFactory.getLogger();

    public static void main(String[] args) {
        Pair<SocketOption, Object>[] socketOptions = new Pair[]{};
        ChannelHandler handler = new ChannelHandler();
        Client client = new Client().connect("localhost", 8080)
                .enableDefaultRead()
                .enableLogging()
                .channelHandler(handler);

        handler.setInput(SIMULATE_NULL_TASK);
        client.run();

        handler.setInput(SIMULATE_NULL_CALLBACK);
        client.run();

        handler.setInput(SIMULATE_NULL_ERROR_CALLBACK);
        client.run();

        handler.setInput(SIMULATE_NULL_INPUT);
        client.run();

        handler.setInput(SIMULATE_EXCEPTION);
        client.run();

        handler.setInput(SIMULATE_SUCCESS);
        client.run();

        handler.setInput(SIMULATE_CLIENT_WORKER);
        client.run();

        handler.setInput(SIMULATE_NEXT_AFTER_ASYNC_CALL);
        client.run();

        handler.setInput(SIMULATE_NEXT_WITH_EXCEPTION);
        client.run();

        handler.setInput(SIMULATE_NEXT);
        client.run();

        handler.setInput(HELLO_WORLD);
        client.run();
    }

    public static class ChannelHandler extends ChannelHandlerAdapter {
        private String input;
        public void setInput(String input) {
            this.input = input;
        }

        @Override
        public void onConnect(Context.OnConnect context, Object data) throws InternalException {
            Allocator.Resource<ByteBuffer> resource = context.allocator().allocate(1024);
            ByteBuffer buffer = resource.get();
            buffer.put(input.getBytes());
            context.write(resource);
            context.flush();
        }

        @Override
        public void onRead(Context.OnRead context, Object data) throws InternalException {
            logger.info("Received: " + Util.toString(data));
        }
    }
}
