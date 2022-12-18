package ramana.example.niotcpserver.example.codec.http;

import ramana.example.niotcpserver.Bootstrap;
import ramana.example.niotcpserver.codec.http.Processor;
import ramana.example.niotcpserver.codec.http.Util;
import ramana.example.niotcpserver.codec.http.handler.CodecChannelHandler;
import ramana.example.niotcpserver.codec.http.handler.ProcessorChannelHandler;
import ramana.example.niotcpserver.codec.http.request.Field;
import ramana.example.niotcpserver.codec.http.request.RequestMessage;
import ramana.example.niotcpserver.codec.http.response.ResponseMessage;
import ramana.example.niotcpserver.log.LogFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpFileServer {
    private static final Logger logger = LogFactory.getLogger();

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
            return new FileServer();
        }
    }

    public static class FileServer implements Processor {
        @Override
        public void process(RequestMessage requestMessage, ResponseMessage responseMessage) {
            switch(requestMessage.method) {
                case Util.METHOD_GET:
                    serve(requestMessage, responseMessage);
                    break;
                case Util.METHOD_HEAD:
                    responseMessage.statusCode = Util.STATUS_OK;
                    break;
                case Util.METHOD_OPTIONS:
                    responseMessage.statusCode = Util.STATUS_OK;
                    responseMessage.headers = allowedMethods();
                    break;
                default:
                    responseMessage.statusCode = Util.STATUS_NOT_IMPLEMENTED;
                    ArrayList<String> values = new ArrayList<>();
                    values.add(String.valueOf(0));
                    responseMessage.headers.add(new Field(Util.REQ_HEADER_CONTENT_LENGTH, values));
            }
        }

        private void serve(RequestMessage requestMessage, ResponseMessage responseMessage) {
            String path = requestMessage.path;
            if(path.equals("")) path = "index.html";
            byte[] content = new byte[0];
            String directory = "/pub_html/";

            InputStream in = getClass().getResourceAsStream(directory + path);
            if(in == null) {
                responseMessage.statusCode = Util.STATUS_NOT_FOUND;
            } else {
                try {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream(in.available());
                    content = new byte[in.available()];
                    responseMessage.statusCode = Util.STATUS_OK;
                    int bytesRead;
                    while((bytesRead =  in.read(content)) != -1) {
                        buffer.write(content, 0, bytesRead);
                    }
                    content = buffer.toByteArray();
                } catch (IOException e) {
                    content = new byte[0];
                    responseMessage.statusCode = Util.STATUS_INTERNAL_SERVER_ERROR;
                } finally {
                    try {
                        in.close();
                    } catch (IOException exception) {
                        logger.log(Level.INFO, exception.getMessage(), exception);
                    }
                }
            }
            ArrayList<String> values = new ArrayList<>();
            responseMessage.body = content;
            values.add(String.valueOf(content.length));
            responseMessage.headers.add(new Field(Util.REQ_HEADER_CONTENT_LENGTH, values));
        }

        private List<Field> allowedMethods() {
            List<Field> headers = new ArrayList<>(1);
            ArrayList<String> values = new ArrayList<>();
            values.add("GET");
            values.add("HEAD");
            values.add("OPTIONS");
            headers.add(new Field("Allow", values));
            values = new ArrayList<>();
            values.add(String.valueOf(0));
            headers.add(new Field(Util.REQ_HEADER_CONTENT_LENGTH, values));
            return headers;
        }
    }
}
