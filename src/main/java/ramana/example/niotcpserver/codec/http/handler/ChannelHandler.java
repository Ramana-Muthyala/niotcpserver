package ramana.example.niotcpserver.codec.http.handler;

import ramana.example.niotcpserver.codec.http.MessageHolder;
import ramana.example.niotcpserver.codec.http.request.RequestCodec;
import ramana.example.niotcpserver.codec.http.response.ResponseCodec;
import ramana.example.niotcpserver.codec.http.response.ResponseMessage;
import ramana.example.niotcpserver.codec.parser.ParseException;
import ramana.example.niotcpserver.handler.Context;
import ramana.example.niotcpserver.handler.impl.ChannelHandlerAdapter;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;

public class ChannelHandler extends ChannelHandlerAdapter {
    private RequestCodec requestCodec;
    private ResponseCodec responseCodec;

    @Override
    public void onConnect(Context.OnConnect context, Object data) throws InternalException {
        if(!context.isDefaultReadEnabled()) context.fireReadInterest();
        requestCodec = new RequestCodec();
        responseCodec = ResponseCodec.getInstance();
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
            MessageHolder messageHolder = new MessageHolder(requestCodec.get());
            context.next(messageHolder);
        }
    }

    @Override
    public void onWrite(Context.OnWrite context, Object data) throws InternalException {
        context.next(responseCodec.encode(context.allocator(), (ResponseMessage) data));
    }
}