package ramana.example.niotcpserver.example.codec.http.v1.ssl;

import ramana.example.niotcpserver.Bootstrap;
import ramana.example.niotcpserver.codec.http.Util;
import ramana.example.niotcpserver.codec.http.handler.v1.CodecChannelHandler;
import ramana.example.niotcpserver.codec.http.handler.v1.ProcessorChannelHandler;
import ramana.example.niotcpserver.codec.http.request.v1.RequestMessage;
import ramana.example.niotcpserver.codec.http.response.ResponseMessage;
import ramana.example.niotcpserver.codec.http.v1.Processor;

import java.util.ArrayList;

public class LatencyTestHttpsServer {

    public static void main(String[] args) {
        new Bootstrap().listen(8443)
                .enableDefaultRead()
                .numOfWorkers(4)
                .enableSsl()
                .channelHandler(CodecChannelHandler.class)
                .channelHandler(ChannelHandler.class)
                .start();
    }

    public static class ChannelHandler extends ProcessorChannelHandler {
        private static final HelloProcessor helloProcessor = new HelloProcessor();
        @Override
        protected Processor create() {
            // Stateless processors do not have to be instantiated for each channel.
            return helloProcessor;
        }
    }

    public static class HelloProcessor implements Processor {
        private static final byte[] message = "Hello World !!".getBytes();
        private static final ArrayList<String> values = new ArrayList<>(1);
        static {
            values.add(String.valueOf(message.length));
        }

        @Override
        public void process(RequestMessage requestMessage, ResponseMessage responseMessage) {
            responseMessage.statusCode = Util.STATUS_OK;
            responseMessage.body = message;
            responseMessage.headers.put(Util.REQ_HEADER_CONTENT_LENGTH, values);
        }
    }
}
