package ramana.example.niotcpserver.example.codec.http.v1;

import ramana.example.niotcpserver.Bootstrap;
import ramana.example.niotcpserver.codec.http.Util;
import ramana.example.niotcpserver.codec.http.handler.v1.CodecChannelHandler;
import ramana.example.niotcpserver.codec.http.handler.v1.ProcessorChannelHandler;
import ramana.example.niotcpserver.codec.http.request.Field;
import ramana.example.niotcpserver.codec.http.request.v1.RequestMessage;
import ramana.example.niotcpserver.codec.http.response.ResponseMessage;
import ramana.example.niotcpserver.codec.http.v1.Processor;
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
        private static final FileServer fileServer = new FileServer();
        @Override
        protected Processor create() {
            // Stateless processors do not have to be instantiated for each channel.
            return fileServer;
        }
    }

    public static class FileServer implements Processor {
        private static final List<Field> allowedMethodsResponseHeaders = new ArrayList<>(2);
        private static final ArrayList<String> allowedMethodsValues = new ArrayList<>();
        private static final ArrayList<String> contentLengthValues = new ArrayList<>(1);
        private static final Field contentLengthHeader = new Field(Util.REQ_HEADER_CONTENT_LENGTH, contentLengthValues);
        static {
            allowedMethodsValues.add("GET");
            allowedMethodsValues.add("HEAD");
            allowedMethodsValues.add("OPTIONS");
            allowedMethodsResponseHeaders.add(new Field("Allow", allowedMethodsValues));
            contentLengthValues.add(String.valueOf(0));
            allowedMethodsResponseHeaders.add(contentLengthHeader);
        }
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
                    responseMessage.headers = allowedMethodsResponseHeaders;
                    break;
                default:
                    responseMessage.statusCode = Util.STATUS_NOT_IMPLEMENTED;
                    responseMessage.headers.add(contentLengthHeader);
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
    }
}
