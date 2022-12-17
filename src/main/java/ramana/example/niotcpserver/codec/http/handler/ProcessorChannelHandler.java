package ramana.example.niotcpserver.codec.http.handler;

import ramana.example.niotcpserver.codec.http.Processor;
import ramana.example.niotcpserver.codec.http.request.RequestMessage;
import ramana.example.niotcpserver.codec.http.response.ResponseMessage;
import ramana.example.niotcpserver.handler.Context;
import ramana.example.niotcpserver.handler.impl.ChannelHandlerAdapter;
import ramana.example.niotcpserver.types.InternalException;

public abstract class ProcessorChannelHandler extends ChannelHandlerAdapter {

    @Override
    public void onRead(Context.OnRead context, Object data) throws InternalException {
        RequestMessage[] requestMessages = (RequestMessage[]) data;
        for(RequestMessage requestMessage : requestMessages) {
            ResponseMessage responseMessage = new ResponseMessage();
            Processor processor = create();
            processor.process(requestMessage, responseMessage);
            context.write(responseMessage);
        }
    }

    protected abstract Processor create();
}
