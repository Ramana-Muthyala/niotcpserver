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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EchoHttpServer {

    public static void main(String[] args) {
        new Bootstrap().listen(8080)
                .enableDefaultRead()
                .enableLogging()
                .channelHandler(CodecChannelHandler.class)
                .channelHandler(ChannelHandler.class)
                .start();
    }

    public static class ChannelHandler extends ProcessorChannelHandler {
        @Override
        protected Processor create() {
            return new EchoProcessor();
        }
    }

    public static class EchoProcessor implements Processor {
        @Override
        public void process(RequestMessage requestMessage, ResponseMessage responseMessage) {
            switch(requestMessage.method) {
                case Util.METHOD_CONNECT:
                    responseMessage.statusCode = Util.STATUS_NOT_IMPLEMENTED;
                    break;
                case Util.METHOD_HEAD:
                    responseMessage.statusCode = Util.STATUS_OK;
                    break;
                case Util.METHOD_OPTIONS:
                    responseMessage.statusCode = Util.STATUS_OK;
                    responseMessage.headers = allowedMethods();
                    break;
                default:
                    echo(requestMessage, responseMessage);
            }
        }

        private void echo(RequestMessage requestMessage, ResponseMessage responseMessage) {
            responseMessage.statusCode = Util.STATUS_OK;
            StringBuilder builder = new StringBuilder();
            builder.append(requestMessage.method).append(" /")
                    .append(requestMessage.path);
            Map<String, String> queryParams = requestMessage.queryParameters;
            if(queryParams.size() > 0) builder.append('?');
            for (Map.Entry<String, String> entry: queryParams.entrySet()) {
                builder.append(entry.getKey()).append('=').append(entry.getValue());
            }
            builder.append(Util.CRLF_STRING);

            for (Field header: requestMessage.headers) {
                builder.append(header.name).append((char)Util.COLON);
                Iterator<String> iterator = header.values.iterator();
                while(iterator.hasNext()) {
                    builder.append(iterator.next());
                    if(iterator.hasNext()) builder.append((char)Util.COMMA);
                }
                builder.append(Util.CRLF_STRING);
            }
            builder.append(Util.CRLF_STRING);

            byte[] statusAndHeaders = builder.toString().getBytes();
            int bodyLength = requestMessage.body == null ? 0 : requestMessage.body.length;
            int capacity = statusAndHeaders.length + bodyLength;
            responseMessage.body = new byte[capacity];
            System.arraycopy(statusAndHeaders, 0, responseMessage.body, 0, statusAndHeaders.length);
            if(bodyLength != 0) {
                System.arraycopy(requestMessage.body, 0, responseMessage.body, statusAndHeaders.length, bodyLength);
            }

            ArrayList<String> values = new ArrayList<>();
            values.add(String.valueOf(capacity));
            responseMessage.headers.add(new Field(Util.REQ_HEADER_CONTENT_LENGTH, values));
        }

        private List<Field> allowedMethods() {
            List<Field> headers = new ArrayList<>(1);
            ArrayList<String> values = new ArrayList<>();
            values.add("GET");
            values.add("HEAD");
            values.add("POST");
            values.add("PUT");
            values.add("DELETE");
            values.add("OPTIONS");
            values.add("TRACE");
            headers.add(new Field("Allow", values));
            values = new ArrayList<>();
            values.add(String.valueOf(0));
            headers.add(new Field(Util.REQ_HEADER_CONTENT_LENGTH, values));
            return headers;
        }
    }
}
