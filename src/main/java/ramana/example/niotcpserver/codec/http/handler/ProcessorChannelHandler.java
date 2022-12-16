package ramana.example.niotcpserver.codec.http.handler;

import ramana.example.niotcpserver.codec.http.Processor;
import ramana.example.niotcpserver.codec.http.RequestResponseHolder;
import ramana.example.niotcpserver.codec.http.request.RequestMessage;
import ramana.example.niotcpserver.codec.http.response.ResponseCodec;
import ramana.example.niotcpserver.codec.http.response.ResponseMessage;
import ramana.example.niotcpserver.handler.Context;
import ramana.example.niotcpserver.handler.impl.ChannelHandlerAdapter;
import ramana.example.niotcpserver.types.InternalException;

public abstract class ProcessorChannelHandler extends ChannelHandlerAdapter {
    private final ResponseCodec responseCodec = ResponseCodec.getInstance();

    @Override
    public void onRead(Context.OnRead context, Object data) throws InternalException {
        RequestMessage[] requestMessages = (RequestMessage[]) data;
        for(RequestMessage requestMessage : requestMessages) {
            RequestResponseHolder requestResponseHolder = new RequestResponseHolder(requestMessage, new ResponseMessage());
            Processor processor = create();
            processor.process(requestResponseHolder);
            context.write(requestResponseHolder.responseMessage);
        }
    }

    protected abstract Processor create();
}
