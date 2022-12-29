package ramana.example.niotcpserver.codec.http.response;

import ramana.example.niotcpserver.codec.http.Util;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class ResponseCodec {
    private static final ResponseCodec self = new ResponseCodec();
    private static final ArrayList<String> contentLengthZero = new ArrayList<>(1);
    static {
        contentLengthZero.add(String.valueOf(0));
    }

    private ResponseCodec() {}

    public static ResponseCodec getInstance() {
        return self;
    }

    public Allocator.Resource<ByteBuffer> badRequest(Allocator<ByteBuffer> allocator) throws InternalException {
        return encode(allocator, badRequest());
    }

    public Allocator.Resource<ByteBuffer> encode(Allocator<ByteBuffer> allocator, ResponseMessage responseMessage) throws InternalException {
        StringBuilder builder = new StringBuilder();
        String statusMessage = Util.statusCodeToText.get(responseMessage.statusCode);
        if(statusMessage == null) statusMessage = Util.statusCodeToText.get(Util.STATUS_INTERNAL_SERVER_ERROR);
        builder.append(statusMessage).append(Util.CRLF_STRING);

        for (Map.Entry<String, ArrayList<String>> header: responseMessage.headers.entrySet()) {
            builder.append(header.getKey()).append((char)Util.COLON);
            Iterator<String> iterator = header.getValue().iterator();
            while(iterator.hasNext()) {
                builder.append(iterator.next());
                if(iterator.hasNext()) builder.append((char)Util.COMMA);
            }
            builder.append(Util.CRLF_STRING);
        }
        builder.append(Util.CRLF_STRING);

        byte[] statusAndHeaders = builder.toString().getBytes();
        int bodyLength = responseMessage.body == null ? 0 : responseMessage.body.length;
        int capacity = statusAndHeaders.length + bodyLength;
        Allocator.Resource<ByteBuffer> data = allocator.allocate(capacity);
        ByteBuffer byteBuffer = data.get();
        byteBuffer.put(statusAndHeaders);
        if(bodyLength > 0) {
            byteBuffer.put(responseMessage.body);
        }

        return data;
    }

    public ResponseMessage badRequest() {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.statusCode = Util.STATUS_BAD_REQUEST;
        responseMessage.headers.put(Util.REQ_HEADER_CONTENT_LENGTH, contentLengthZero);
        return responseMessage;
    }
}
