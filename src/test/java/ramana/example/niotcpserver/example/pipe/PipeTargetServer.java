package ramana.example.niotcpserver.example.pipe;

import ramana.example.niotcpserver.Bootstrap;
import ramana.example.niotcpserver.handler.Context;
import ramana.example.niotcpserver.handler.impl.ChannelHandlerAdapter;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;
import ramana.example.niotcpserver.util.Util;

import java.nio.ByteBuffer;

public class PipeTargetServer {

    public static void main(String[] args) {
        new Bootstrap().listen(8081)
                .enableDefaultRead()
                .enableLogging()
                .channelHandler(ChannelHandler.class)
                .start();
    }

    public static class ChannelHandler extends ChannelHandlerAdapter {
        @Override
        public void onRead(Context.OnRead context, Object data) throws InternalException {
            Allocator.Resource<ByteBuffer> resource = (Allocator.Resource<ByteBuffer>) data;
            ByteBuffer byteBuffer = resource.get();
            String receivedData = Util.toString(data);
            byteBuffer.clear(); // reuse
            byteBuffer.put(("DemoServerTwo: Echoing: " + receivedData).getBytes());
            context.write(resource);
            context.close();
        }
    }
}
