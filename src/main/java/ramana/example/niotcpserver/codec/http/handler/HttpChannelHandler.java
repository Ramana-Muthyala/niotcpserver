package ramana.example.niotcpserver.codec.http.handler;

import ramana.example.niotcpserver.codec.http.HttpMessageHolder;
import ramana.example.niotcpserver.codec.http.request.HttpRequestCodec;
import ramana.example.niotcpserver.codec.http.response.HttpResponseCodec;
import ramana.example.niotcpserver.codec.http.response.HttpResponseMessage;
import ramana.example.niotcpserver.codec.parser.ParseException;
import ramana.example.niotcpserver.handler.Context;
import ramana.example.niotcpserver.handler.impl.ChannelHandlerAdapter;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;

/*
 * HTTP/1.1 - ref: https://httpwg.org/specs/rfc9112.html
 * Though the reference is given above. It may not be fully implemented.
 * It is implemented to some extent and can evolve incrementally.
 */
public class HttpChannelHandler extends ChannelHandlerAdapter {
    private HttpRequestCodec requestCodec;
    private HttpResponseCodec responseCodec;

    @Override
    public void onConnect(Context.OnConnect context, Object data) throws InternalException {
        if(!context.isDefaultReadEnabled()) context.fireReadInterest();
        requestCodec = new HttpRequestCodec();
        responseCodec = HttpResponseCodec.getInstance();
    }

    @Override
    public void onRead(Context.OnRead context, Object data) throws InternalException {
        try {
            requestCodec.decode((Allocator.Resource< ByteBuffer >)data);
        } catch (ParseException e) {
            context.write(responseCodec.badRequest(context.allocator()));
            return;
        }
        if(requestCodec.isDecoded()) {
            HttpMessageHolder messageHolder = new HttpMessageHolder(requestCodec.get());
            context.next(messageHolder);
        }
    }

    @Override
    public void onWrite(Context.OnWrite context, Object data) throws InternalException {
        context.next(responseCodec.encode(context.allocator(), (HttpResponseMessage) data));
    }
}
