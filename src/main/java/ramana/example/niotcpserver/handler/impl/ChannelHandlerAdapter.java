package ramana.example.niotcpserver.handler.impl;

import ramana.example.niotcpserver.handler.ChannelHandler;
import ramana.example.niotcpserver.handler.Context;
import ramana.example.niotcpserver.types.InternalException;

public class ChannelHandlerAdapter implements ChannelHandler {

    @Override
    public void onConnect(Context.OnConnect context, Object data) throws InternalException {
        context.next(data);
    }

    @Override
    public void onRead(Context.OnRead context, Object data) throws InternalException {
        context.next(data);
    }

    @Override
    public void onReadComplete(Context.OnRead context, Object data) throws InternalException {
        context.next(data);
    }

    @Override
    public void onWrite(Context.OnWrite context, Object data) throws InternalException {
        context.next(data);
    }

    @Override
    public void onClose(Context.OnClose context, Object data, Throwable cause) throws InternalException {
        context.next(data, cause);
    }
}
