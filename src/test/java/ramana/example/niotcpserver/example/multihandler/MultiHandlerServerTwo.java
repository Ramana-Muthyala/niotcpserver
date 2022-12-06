package ramana.example.niotcpserver.example.multihandler;

import ramana.example.niotcpserver.Bootstrap;
import ramana.example.niotcpserver.handler.Context;
import ramana.example.niotcpserver.handler.impl.ChannelHandlerAdapter;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;
import ramana.example.niotcpserver.util.Util;

import java.nio.ByteBuffer;

public class MultiHandlerServerTwo {

    public static void main(String[] args) {
        new Bootstrap().listen(8080)
                .enableDefaultRead()
                .enableLogging()
                .channelHandler(ChannelHandlerOne.class)
                .channelHandler(ChannelHandlerTwo.class)
                .start();
    }

    public static class ChannelHandlerOne extends ChannelHandlerAdapter {
        @Override
        public void onRead(Context.OnRead context, Object data) throws InternalException {
            context.next(Util.toString(data));
        }

        @Override
        public void onWrite(Context.OnWrite context, Object data) throws InternalException {
            Allocator.Resource<ByteBuffer> resource = context.allocator().allocate(1024);
            ByteBuffer buffer = resource.get();
            buffer.put(("ChannelHandlerOne: " + data.toString()).getBytes());
            context.write(resource);
            context.close();
        }
    }

    public static class ChannelHandlerTwo extends ChannelHandlerAdapter {
        @Override
        public void onRead(Context.OnRead context, Object data) throws InternalException {
            context.write("ChannelHandlerTwo: " + data);
        }
    }
}
