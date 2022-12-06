package ramana.example.niotcpserver.example.negativetests;

import ramana.example.niotcpserver.Bootstrap;
import ramana.example.niotcpserver.handler.Context;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;

public class NegativeTestServer {

    public static void main(String[] args) {
        new Bootstrap().listen(8080)
                .enableDefaultRead()
                .enableLogging()
                .channelHandler(ChannelHandler.class)
                .start();
    }

    public static class ChannelHandler implements ramana.example.niotcpserver.handler.ChannelHandler {

        @Override
        public void onConnect(Context.OnConnect context, Object data) throws InternalException {
            context.next(data);
        }

        @Override
        public void onRead(Context.OnRead context, Object data) throws InternalException {
            Allocator.Resource<ByteBuffer> resource = context.allocator().allocate(1024);
            ByteBuffer buffer = resource.get();
            String greeting = "Hello World !!";
            buffer.put(greeting.getBytes());
            context.write(resource);
            context.flush();
            context.next(data);
            context.close();   // This will be ignored
        }

        @Override
        public void onReadComplete(Context.OnRead context, Object data) throws InternalException {
            context.next(data);
            context.close();   // This will be ignored
            context.write("Hello World !!");   // This will be ignored
        }

        @Override
        public void onWrite(Context.OnWrite context, Object data) throws InternalException {
            context.next(data);
        }

        @Override
        public void onClose(Context.OnClose context, Object data, Throwable cause) throws InternalException {
            context.next(data, cause);
        }
    }
}
