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
                .channelHandler(CodecChannelHandler.class)
                .channelHandler(ChannelHandler.class)
                .start();
    }

    public static class ChannelHandler extends ProcessorChannelHandler {
        @Override
        protected Processor create() {
            return new HelloProcessor();
        }
    }

    public static class HelloProcessor implements Processor {
        @Override
        public void process(RequestMessage requestMessage, ResponseMessage responseMessage) {
            responseMessage.statusCode = Util.STATUS_OK;
            responseMessage.body = "Hello World !!".getBytes();
            ArrayList<String> values = new ArrayList<>(1);
            values.add(String.valueOf(responseMessage.body.length));
            responseMessage.headers.add(new Field(Util.REQ_HEADER_CONTENT_LENGTH, values));
        }
    }
}
