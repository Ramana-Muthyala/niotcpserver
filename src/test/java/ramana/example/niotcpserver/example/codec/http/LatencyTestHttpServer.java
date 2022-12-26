package ramana.example.niotcpserver.example.codec.http;

import ramana.example.niotcpserver.Bootstrap;
import ramana.example.niotcpserver.codec.http.Processor;
import ramana.example.niotcpserver.codec.http.Util;
import ramana.example.niotcpserver.codec.http.handler.CodecChannelHandler;
import ramana.example.niotcpserver.codec.http.handler.ProcessorChannelHandler;
import ramana.example.niotcpserver.codec.http.request.Field;
import ramana.example.niotcpserver.codec.http.request.RequestMessage;
import ramana.example.niotcpserver.codec.http.response.ResponseMessage;

import java.util.ArrayList;

public class LatencyTestHttpServer {

    public static void main(String[] args) {
        new Bootstrap().listen(8080)
                .enableDefaultRead()
                .numOfWorkers(4)
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
        private static final Field contentLengthHeader = new Field(Util.REQ_HEADER_CONTENT_LENGTH, values);
        @Override
        public void process(RequestMessage requestMessage, ResponseMessage responseMessage) {
            responseMessage.statusCode = Util.STATUS_OK;
            responseMessage.body = message;
            responseMessage.headers.add(contentLengthHeader);
        }
    }
}
