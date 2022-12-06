package ramana.example.niotcpserver.types;

import ramana.example.niotcpserver.io.Allocator;

import java.nio.ByteBuffer;

public class InternalException extends Exception {
    public InternalException(Throwable cause) {
        super(cause);
    }

    public InternalException(String message) {
        super(message);
    }

    public static final String CHANNEL_CLOSED = "Channel closed";
    public static String ILLEGAL_ARGUMENT_FOR_WRITE = "Illegal argument: Expected: " + Allocator.Resource.class.getName() + "<" + ByteBuffer.class.getName() +  ">. Found: ";
    public static final String RESOURCE_RELEASED = "Resource released";
}
