package ramana.example.niotcpserver.codec.http.request.v1;

import ramana.example.niotcpserver.codec.http.Util;
import ramana.example.niotcpserver.codec.http.request.Field;
import ramana.example.niotcpserver.codec.parser.ParseException;
import ramana.example.niotcpserver.codec.parser.v1.AbstractStateParser;
import ramana.example.niotcpserver.codec.parser.v1.Buffer;
import ramana.example.niotcpserver.codec.parser.v1.ByteParser;
import ramana.example.niotcpserver.codec.parser.v1.StringAccumulatorParser;
import ramana.example.niotcpserver.types.InternalException;

import java.util.ArrayList;

public class HeaderParser extends AbstractStateParser {
    private final RequestMessage requestMessage;
    private final ArrayList<Field> headers;
    private State state = State.HEADER_NAME;
    private final StringAccumulatorParser nameParser = new StringAccumulatorParser(Util.COLON, Util.CR, Util.REQ_FIELD_MAX_LEN, Util.REQ_FIELD_MAX_LEN);
    private final StringAccumulatorParser valueParser = new StringAccumulatorParser(Util.COMMA, Util.CR, Util.REQ_FIELD_VAL_MAX_LEN, Util.REQ_FIELD_VAL_MAX_LEN);
    private String name;
    private ArrayList<String> values = new ArrayList<>();

    public HeaderParser(RequestMessage requestMessage) {
        this.headers = new ArrayList<>();
        this.requestMessage = requestMessage;
        requestMessage.headers = headers;
    }

    @Override
    public void parse(Buffer buffer) throws ParseException, InternalException {
        ByteParser.Status status;
        while (state != State.END) {
            if(!buffer.hasRemaining()) return;

            switch (state) {
                case HEADER_NAME:
                    status = nameParser.parse(buffer);
                    if(status == ByteParser.Status.DONE) {
                        if(nameParser.getLastRead() == Util.COLON) {
                            name = nameParser.getResult();
                            if(name.isEmpty()) throw new ParseException();
                            nameParser.reset();
                            state = State.HEADER_VALUE;
                        } else {
                            state = State.END;
                        }
                    } else if(status == ByteParser.Status.FAIL) throw new ParseException();
                    break;

                case HEADER_VALUE:
                    status = valueParser.parse(buffer);
                    if(status == ByteParser.Status.DONE) {
                        String value = valueParser.getResult();
                        if(value.isEmpty()) throw new ParseException();
                        values.add(value);
                        if(valueParser.getLastRead() == Util.CR) {
                            headers.add(new Field(name, values));
                            values = new ArrayList<>();
                            state = State.HEADER_END;
                        }
                        valueParser.reset();
                    } else if(status == ByteParser.Status.FAIL) throw new ParseException();
                    break;

                case HEADER_END:
                    // already checked buffer.hasRemaining() in while loop
                    byte tmp = buffer.read();
                    if(tmp != Util.LF) throw new ParseException();
                    state = State.HEADER_NAME;
                    break;
            }
        }

        if(buffer.hasRemaining()) {
            byte tmp = buffer.read();
            if(tmp != Util.LF) throw new ParseException();
            done = true;
            next = null;
            Integer contentLength = null;
            ArrayList<String> transferEncoding = null;
            for (Field header: headers) {
                if(Util.REQ_HEADER_CONTENT_LENGTH.equals(header.name)) {
                    try {
                        contentLength = Integer.parseInt(header.values.get(0));
                    } catch (IndexOutOfBoundsException | NumberFormatException exception) {
                        throw new ParseException(exception);
                    }
                } else if(Util.REQ_HEADER_TRANSFER_ENCODING.equals(header.name)) {
                    transferEncoding = header.values;
                }
                if(contentLength != null  &&  transferEncoding != null) throw new ParseException();
            }
            if(transferEncoding != null  &&  transferEncoding.contains(Util.REQ_HEADER_TRANSFER_ENCODING_CHUNKED)) {
                next = new ChunkedBodyParser(requestMessage);
            }
            if(contentLength != null) {
                if(contentLength < 0) throw new ParseException();
                if(contentLength > 0) {
                    next = new ContentLengthParser(requestMessage, contentLength);
                }
            }
        }
    }

    private enum State { HEADER_NAME, HEADER_VALUE, HEADER_END, END }
}
