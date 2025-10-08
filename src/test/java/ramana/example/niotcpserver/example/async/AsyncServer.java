package ramana.example.niotcpserver.example.async;

import ramana.example.niotcpserver.Bootstrap;
import ramana.example.niotcpserver.Client;
import ramana.example.niotcpserver.handler.Context;
import ramana.example.niotcpserver.handler.impl.ChannelHandlerAdapter;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.log.LogFactory;
import ramana.example.niotcpserver.types.InternalException;
import ramana.example.niotcpserver.types.Pair;
import ramana.example.niotcpserver.types.TriConsumer;
import ramana.example.niotcpserver.util.Util;

import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.logging.Logger;

public class AsyncServer {
    private static final Logger logger = LogFactory.getLogger();
    public static void main(String[] args) {
        Pair<SocketOption, Object>[] serverSocketOptions = new Pair[]{};
        Pair<SocketOption, Object>[] socketOptions = new Pair[]{};
        new Bootstrap().listen(8080)
                .backlog(100)
                .serverSocketOptions(serverSocketOptions)
                .numOfWorkers(4)
                .socketOptions(socketOptions)
                .enableDefaultRead()
                .enableLogging()
                .enableAsync(2)
                .channelHandler(ChannelHandlerOne.class)
                .channelHandler(ChannelHandlerTwo.class)
                .start();
    }

    public static final String SIMULATE_NULL_TASK = "SIMULATE_NULL_TASK";
    public static final String SIMULATE_NULL_CALLBACK = "SIMULATE_NULL_CALLBACK";
    public static final String SIMULATE_NULL_ERROR_CALLBACK = "SIMULATE_NULL_ERROR_CALLBACK";
    public static final String SIMULATE_NULL_INPUT = "SIMULATE_NULL_INPUT";
    public static final String SIMULATE_EXCEPTION = "SIMULATE_EXCEPTION";
    public static final String SIMULATE_SUCCESS = "SIMULATE_SUCCESS";
    public static final String SIMULATE_CLIENT_WORKER = "SIMULATE_CLIENT_WORKER";
    public static final String SIMULATE_NEXT_AFTER_ASYNC_CALL = "SIMULATE_NEXT_AFTER_ASYNC_CALL";
    public static final String SIMULATE_NEXT_WITH_EXCEPTION = "SIMULATE_NEXT_WITH_EXCEPTION";
    public static final String SIMULATE_NEXT = "SIMULATE_NEXT";

    public static final String HELLO_WORLD = "Hello World !!";

    public static class ChannelHandlerOne extends ChannelHandlerAdapter {
        @Override
        public void onRead(Context.OnRead context, Object data) throws InternalException {
            String input = Util.toString(data);

            TriConsumer<String, String, Context.OnWrite> callback0 = (message, result, context0) -> {
                logger.info(message + result);
                Allocator.Resource<ByteBuffer> resource = context0.allocator().allocate(1024);
                try {
                    ByteBuffer buffer = resource.get();
                    buffer.put(result.getBytes());
                    context0.write(resource);
                    context0.close();
                } catch (InternalException exception) {
                    throw new RuntimeException(exception);
                }
            };

            TriConsumer<String, String, Context.OnWrite> callbackNext0 = (message, result, context0) -> {
                logger.info(message + result);
                try {
                    context0.next(result);
                } catch (InternalException exception) {
                    throw new RuntimeException(exception);
                }
            };

            Function<String, String> task = (input0) -> "Processed in thread: " + Thread.currentThread() + ", Input: " + input0;
            Function<String, String> exceptionTask = (input0) -> {
                throw new RuntimeException(input0);
            };
            BiConsumer<String, Context.OnWrite> callback = (result, context0) -> callback0.accept("callback: Result: ", result, context0);
            TriConsumer<String, Context.OnWrite, Exception> errorCallback = (input0, context0, exception) -> callback0.accept("errorCallback: Input: " + input0 + ", ", "Exception: " + exception.getMessage(), context0);
            BiConsumer<String, Context.OnWrite> callbackNext = (result, context0) -> callbackNext0.accept("callback: Result: ", result, context0);
            TriConsumer<String, Context.OnWrite, Exception> errorCallbackNext = (input0, context0, exception) -> callbackNext0.accept("errorCallback: Input: " + input0 + ", ", "Exception: " + exception.getMessage(), context0);

            switch (input) {
                case SIMULATE_NULL_TASK:
                    context.async(null, null, callback, errorCallback);
                    break;
                case SIMULATE_NULL_CALLBACK:
                    context.async(null, task, null, errorCallback);
                    break;
                case SIMULATE_NULL_ERROR_CALLBACK:
                    context.async(null, task, callback, null);
                    break;
                case SIMULATE_NULL_INPUT:
                    context.async(null, task, callback, errorCallback);
                    break;
                case SIMULATE_EXCEPTION:
                    context.async(SIMULATE_EXCEPTION, exceptionTask, callback, errorCallback);
                    break;
                case SIMULATE_SUCCESS:
                    context.async(SIMULATE_SUCCESS, task, callback, errorCallback);
                    break;
                case SIMULATE_CLIENT_WORKER:
                    context.async(SIMULATE_CLIENT_WORKER, createClientTask(), callback, errorCallback);
                    break;
                case SIMULATE_NEXT_AFTER_ASYNC_CALL:
                    context.async(SIMULATE_NEXT_AFTER_ASYNC_CALL, task, callback, errorCallback);
                    // This next method call will be ignored. NoOp.
                    context.next(SIMULATE_NEXT_AFTER_ASYNC_CALL);
                    break;
                case SIMULATE_NEXT_WITH_EXCEPTION:
                    context.async(SIMULATE_NEXT_WITH_EXCEPTION, exceptionTask, callbackNext, errorCallbackNext);
                    break;
                case SIMULATE_NEXT:
                    context.async(SIMULATE_NEXT, task, callbackNext, errorCallbackNext);
                    break;
                default:
                    context.async(HELLO_WORLD, task, callback, errorCallback);
            }
        }

        private Function<String, String> createClientTask() {
            // When you are using client worker in async, do not use a separate thread.
            // i.e. do not invoke start() method. Just invoke run() method.
            // It will run in async pool thread.
            return (input) -> {
                ClientChannelHandler handler = new ClientChannelHandler();
                handler.input = input;
                handler.host = "www.google.com";
                new Client().connect(handler.host, 443)
                        .enableDefaultRead()
                        .enableLogging()
                        .enableSslWithDefaultTrustManager()
                        .channelHandler(handler)
                        .run();
                return handler.getResult();
            };
        }
    }

    private static class ClientChannelHandler extends ChannelHandlerAdapter {
        private String input;
        private String host;
        private String response;

        @Override
        public void onConnect(Context.OnConnect context, Object data) throws InternalException {
            Allocator.Resource<ByteBuffer> resource = context.allocator().allocate(1024);
            ByteBuffer buffer = resource.get();
            String request = "GET / HTTP/1.1\r\n" +
                    "Host:" + host + "\r\n" +
                    "User-Agent:NIOTCPServer/1.0\r\n" +
                    "Accept:*/*\r\n" +
                    "\r\n";
            buffer.put(request.getBytes());
            context.write(resource);
            context.flush();
        }

        @Override
        public void onRead(Context.OnRead context, Object data) throws InternalException {
            String STATUS_200_OK = "HTTP/1.1 200 OK";
            response = Util.toString(data);
            if(response.startsWith(STATUS_200_OK)) {
                response = STATUS_200_OK;
            }
            context.close();
        }

        public String getResult() {
            return "Input: " + input + ": Host: " + host + ", Response: " + response;
        }
    }

    public static class ChannelHandlerTwo extends ChannelHandlerAdapter {
        @Override
        public void onRead(Context.OnRead context, Object data) throws InternalException {
            logger.info(data.toString());
            Allocator.Resource<ByteBuffer> resource = context.allocator().allocate(1024);
            try {
                ByteBuffer buffer = resource.get();
                buffer.put(data.toString().getBytes());
                context.write(resource);
                context.close();
            } catch (InternalException exception) {
                throw new RuntimeException(exception);
            }
        }
    }
}
