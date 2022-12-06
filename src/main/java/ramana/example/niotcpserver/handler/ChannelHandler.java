package ramana.example.niotcpserver.handler;

import ramana.example.niotcpserver.types.InternalException;

public interface ChannelHandler {
    void onConnect(Context.OnConnect context, Object data) throws InternalException;
    void onRead(Context.OnRead context, Object data) throws InternalException;
    void onReadComplete(Context.OnRead context, Object data) throws InternalException;
    void onWrite(Context.OnWrite context, Object data) throws InternalException;
    void onClose(Context.OnClose context, Object data, Throwable cause) throws InternalException;
}
