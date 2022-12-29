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
import java.util.Map;

public class TrailerParser extends AbstractStateParser {
    private static final byte[] nameParserDelimiters = new byte[] { Util.COLON, Util.CR };
    private static final byte[] valueParserDelimiters = new byte[] { Util.COMMA, Util.CR };
    private final ArrayList<Field> trailers;
    private final Map<String, ArrayList<String>> headers;
    private State state = State.TRAILER_NAME;
    private final StringAccumulatorParser nameParser = new StringAccumulatorParser(nameParserDelimiters, Util.REQ_FIELD_MAX_LEN, Util.REQ_FIELD_MAX_LEN);
    private final StringAccumulatorParser valueParser = new StringAccumulatorParser(valueParserDelimiters, Util.REQ_FIELD_VAL_MAX_LEN, Util.REQ_FIELD_VAL_MAX_LEN);
    private String name;
    private ArrayList<String> values = new ArrayList<>();

    public TrailerParser(RequestMessage requestMessage) {
        headers = requestMessage.headers;
        trailers = new ArrayList<>();
    }

    @Override
    public void parse(Buffer buffer) throws ParseException, InternalException {
        ByteParser.Status status;
        while (state != State.END) {
            if(!buffer.hasRemaining()) return;

            switch (state) {
                case TRAILER_NAME:
                    status = nameParser.parse(buffer);
                    if(status == ByteParser.Status.DONE) {
                        if(nameParser.getLastRead() == Util.COLON) {
                            name = nameParser.getResult();
                            if(name.isEmpty()) throw new ParseException();
                            nameParser.reset();
                            state = State.TRAILER_VALUE;
                        } else {
                            state = State.END;
                        }
                    } else if(status == ByteParser.Status.FAIL) throw new ParseException();
                    break;

                case TRAILER_VALUE:
                    status = valueParser.parse(buffer);
                    if(status == ByteParser.Status.DONE) {
                        String value = valueParser.getResult();
                        if(value.isEmpty()) throw new ParseException();
                        values.add(value);
                        if(valueParser.getLastRead() == Util.CR) {
                            trailers.add(new Field(name, values));
                            values = new ArrayList<>();
                            state = State.TRAILER_END;
                        }
                        valueParser.reset();
                    } else if(status == ByteParser.Status.FAIL) throw new ParseException();
                    break;

                case TRAILER_END:
                    // already checked buffer.hasRemaining() in while loop
                    byte tmp = buffer.read();
                    if(tmp != Util.LF) throw new ParseException();
                    state = State.TRAILER_NAME;
                    break;
            }
        }

        if(buffer.hasRemaining()) {
            byte tmp = buffer.read();
            if(tmp != Util.LF) throw new ParseException();
            done = true;
            next = null;

            for (Field trailer: trailers) {
                if(!headers.containsKey(trailer.name)) headers.put(trailer.name, trailer.values);
            }
            headers.get(Util.REQ_HEADER_TRANSFER_ENCODING).remove(Util.REQ_HEADER_TRANSFER_ENCODING_CHUNKED);
        }
    }

    private enum State {TRAILER_NAME, TRAILER_VALUE, TRAILER_END, END }
}
