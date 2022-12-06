package ramana.example.niotcpserver.example.discard;

import ramana.example.niotcpserver.Client;
import ramana.example.niotcpserver.handler.Context;
import ramana.example.niotcpserver.handler.impl.ChannelHandlerAdapter;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;

public class DiscardClient {

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
            for (int i = 0; i < 10; i++) {
                Allocator.Resource<ByteBuffer> resource = context.allocator().allocate(1024);
                ByteBuffer buffer = resource.get();
                String greeting = "Hello World !!";
                buffer.put(greeting.getBytes());
                context.write(resource);
                context.flush();
            }
            context.close();
        }
    }
}
