package ramana.example.niotcpserver.codec.http.request.v1;

import ramana.example.niotcpserver.codec.http.Util;
import ramana.example.niotcpserver.codec.parser.ParseException;
import ramana.example.niotcpserver.codec.parser.v1.AbstractStateParser;
import ramana.example.niotcpserver.codec.parser.v1.Buffer;
import ramana.example.niotcpserver.codec.parser.v1.ByteParser;
import ramana.example.niotcpserver.codec.parser.v1.StringAccumulatorParser;
import ramana.example.niotcpserver.types.InternalException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HeaderParser extends AbstractStateParser {
    private static final byte[] nameParserDelimiters = new byte[] { Util.COLON, Util.CR };
    private static final byte[] valueParserDelimiters = new byte[] { Util.COMMA, Util.CR };
    private final RequestMessage requestMessage;
    private final Map<String, ArrayList<String>> headers;
    private State state = State.HEADER_NAME;
    private final StringAccumulatorParser nameParser = new StringAccumulatorParser(nameParserDelimiters, Util.REQ_FIELD_MAX_LEN, Util.REQ_FIELD_MAX_LEN);
    private final StringAccumulatorParser valueParser = new StringAccumulatorParser(valueParserDelimiters, Util.REQ_FIELD_VAL_MAX_LEN, Util.REQ_FIELD_VAL_MAX_LEN);
    private String name;
    private ArrayList<String> values = new ArrayList<>();

    public HeaderParser(RequestMessage requestMessage) {
        this.headers = new HashMap<>();
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
                        String value = valueParser.getResult().trim();
                        if(value.isEmpty()) throw new ParseException();
                        values.add(value);
                        if(valueParser.getLastRead() == Util.CR) {
                            headers.put(name, values);
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
            ArrayList<String> contentLength = headers.get(Util.REQ_HEADER_CONTENT_LENGTH);
            ArrayList<String> transferEncoding = headers.get(Util.REQ_HEADER_TRANSFER_ENCODING);
            if(contentLength != null  &&  transferEncoding != null) throw new ParseException();
            if(transferEncoding != null  &&  transferEncoding.contains(Util.REQ_HEADER_TRANSFER_ENCODING_CHUNKED)) {
                next = new ChunkedBodyParser(requestMessage);
                return;
            }
            if(contentLength != null) {
                try {
                    int contentLengthInt = Integer.parseInt(contentLength.get(0));
                    if(contentLengthInt < 0) throw new ParseException();
                    if(contentLengthInt > 0) {
                        next = new ContentLengthParser(requestMessage, contentLengthInt);
                    }
                } catch (IndexOutOfBoundsException | NumberFormatException exception) {
                    throw new ParseException(exception);
                }
            }
        }
    }

    private enum State { HEADER_NAME, HEADER_VALUE, HEADER_END, END }
}
