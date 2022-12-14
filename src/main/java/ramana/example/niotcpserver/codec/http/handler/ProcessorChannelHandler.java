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
            ResponseMessage responseMessage = new ResponseMessage();
            switch (validate(requestMessage)) {
                case VALID:
                    RequestResponseHolder requestResponseHolder = new RequestResponseHolder(requestMessage, responseMessage);
                    Processor processor = create();
                    processor.process(requestResponseHolder);
                    context.write(requestResponseHolder.responseMessage);
                    break;
                case INVALID:
                    context.write(responseCodec.badRequest(responseMessage));
                    break;
                case NOT_IMPLEMENTED:
                    context.write(responseCodec.notImplemented(responseMessage));
            }
        }
    }

    protected abstract Processor create();

    private RequestState validate(RequestMessage requestMessage) {
        return RequestState.NOT_IMPLEMENTED;
    }

    private enum RequestState {VALID, INVALID, NOT_IMPLEMENTED}
}
