package ramana.example.niotcpserver.codec.http.request.v1;

import ramana.example.niotcpserver.codec.http.Util;
import ramana.example.niotcpserver.codec.parser.Accumulator;
import ramana.example.niotcpserver.codec.parser.ParseException;
import ramana.example.niotcpserver.codec.parser.v1.*;
import ramana.example.niotcpserver.types.InternalException;

import java.util.HashMap;

public class PathParser extends AbstractStateParser {
    private final RequestMessage requestMessage;
    private Byte state;
    private final ByteSequence httpSlash = new ByteSequence(Util.HTTP_SLASH);
    private final Accumulator accumulator = new Accumulator(Util.REQ_TARGET_MAX_LEN);
    private OriginForm originForm;
    private boolean asteriskFormSpaceConsumed;
    private boolean httpSlashDone;
    private byte httpVersionState;
    private ByteSequence absoluteFormSchema;
    private boolean originFormParsed;
    byte absoluteFormState;
    private SkipParser skipParser;

    public PathParser(RequestMessage requestMessage) {
        this.requestMessage = requestMessage;
    }

    @Override
    public void parse(Buffer buffer) throws ParseException, InternalException {
        if(state == null) {
            if(buffer.hasRemaining()) state = buffer.read();
        }
        if(state == null) return;

        switch (state) {
            case '/':
                parseOriginForm(buffer);
                break;
            case 'h':
                parseAbsoluteForm(buffer);
                break;
            case '*':
                parseAsteriskForm(buffer);
                break;
            default:
                parseAuthorityForm(buffer);
        }

    }

    private void parseAuthorityForm(Buffer buffer) throws ParseException {
        // authority-form: CONNECT www.example.com:80 HTTP/1.1
        // not implementing this as of now.
        // parse exception will result in bad request thrown to the client.
        throw new ParseException();
    }

    private void parseAsteriskForm(Buffer buffer) throws InternalException, ParseException {
        // asterisk-form: OPTIONS * HTTP/1.1
        requestMessage.path = "*";
        if(!asteriskFormSpaceConsumed) {
            if(!buffer.hasRemaining()) return;
            byte current = buffer.read();
            if(current != Util.SP) throw new ParseException();
            asteriskFormSpaceConsumed = true;
        }
        parseSuffix(buffer);
    }

    private void parseSuffix(Buffer buffer) throws InternalException, ParseException {
        if(!httpSlashDone) {
            ByteParser.Status status = httpSlash.parse(buffer);
            if(status == ByteParser.Status.DONE) {
                httpSlashDone = true;
            } else if(status == ByteParser.Status.FAIL) {
                throw new ParseException();
            }
        }

        if(httpSlashDone) {
            while(buffer.hasRemaining()) {
                byte current = buffer.read();
                switch (httpVersionState) {
                    case 0:
                    case 2:
                        if(current < Util.ZERO  ||  current > Util.NINE) throw new ParseException();
                        httpVersionState++;
                        break;
                    case 1:
                        if(current != Util.DOT) throw new ParseException();
                        httpVersionState++;
                        break;
                    case 3:
                        if(current != Util.CR) throw new ParseException();
                        httpVersionState++;
                        break;
                    case 4:
                        if(current != Util.LF) throw new ParseException();
                        done = true;
                        next = new HeaderParser(requestMessage);
                        return;
                }
            }
        }
    }

    private void parseAbsoluteForm(Buffer buffer) throws InternalException, ParseException {
        // absolute-form: GET http://www.example.org/pub/WWW/TheProject.html HTTP/1.1
        // Not implementing exactly as per the spec (as it is time-consuming).
        // Not implementing https as SSL support is not added yet.
        // Implementing in a manner that works for valid requests. Other cases will receive bad request.
        // Can evolve incrementally without code bloat.

        if(absoluteFormState == 0) {
            if(absoluteFormSchema == null) absoluteFormSchema = new ByteSequence(Util.ABSOLUTE_FORM_SCHEMA);
            ByteParser.Status status = absoluteFormSchema.parse(buffer);
            if(status == ByteParser.Status.DONE) absoluteFormState++;
            else if(status == ByteParser.Status.FAIL) throw new ParseException();
        }

        if(absoluteFormState == 1) {
            if(skipParser == null) skipParser = new SkipParser(Util.SLASH, Util.SP, Util.REQ_TARGET_MAX_LEN);
            ByteParser.Status status = skipParser.parse(buffer);
            if(status == ByteParser.Status.DONE) {
                Byte result = skipParser.getResult();
                if(result == Util.SLASH) absoluteFormState++;
                else absoluteFormState += 2;
            } else if(status == ByteParser.Status.FAIL) throw new ParseException();
        }

        if(absoluteFormState == 2) {
            parseOriginForm(buffer);
        }

        if(absoluteFormState == 3) {
            requestMessage.path = "";
            requestMessage.queryParameters = new HashMap<>();
            parseSuffix(buffer);
        }
    }

    private void parseOriginForm(Buffer buffer) throws InternalException, ParseException {
        // origin-form: GET /where?q=now HTTP/1.1
        if(!originFormParsed) {
            if(originForm == null) originForm = new OriginForm(requestMessage, accumulator);
            ByteParser.Status status = originForm.parse(buffer);
            if(status == ByteParser.Status.DONE) {
                originFormParsed = true;
                if(originForm.current != Util.SP) throw new ParseException();
            }
        }
        if(originFormParsed) {
            parseSuffix(buffer);
        }
    }

    private static class OriginForm implements ByteParser<RequestMessage> {
        private final RequestMessage requestMessage;
        private final Accumulator accumulator;
        private State state = State.INITIAL;
        private final HashMap<String, String> queryParams = new HashMap<>();
        private String queryParamName;
        private byte current;

        public OriginForm(RequestMessage requestMessage, Accumulator accumulator) {
            this.requestMessage = requestMessage;
            this.accumulator = accumulator;
        }

        @Override
        public Status parse(Buffer buffer) throws InternalException {
            while(state != State.END) {
                if(!buffer.hasRemaining()) return Status.IN_PROGRESS;
                current = buffer.read();
                switch (state) {
                    case INITIAL:
                        if(current == Util.QUESTION_MARK) {
                            requestMessage.path = accumulator.getAsString();
                            accumulator.reset();
                            state = State.QUERY_PARAM_NAME;
                        } else if (current == Util.SP) {
                            requestMessage.path = accumulator.getAsString();
                            state = State.END;
                        } else {
                            accumulator.put(current);
                        }
                        break;

                    case QUERY_PARAM_NAME:
                        if(current == Util.EQUAL_TO) {
                            queryParamName = accumulator.getAsString();
                            accumulator.reset();
                            state = State.QUERY_PARAM_VALUE;
                        } else if(current == Util.AMPERSAND) {
                            queryParamName = accumulator.getAsString();
                            if(!queryParamName.isEmpty()) {
                                queryParams.put(queryParamName, "");
                            }
                            accumulator.reset();
                        } else if(current == Util.SP) {
                            queryParamName = accumulator.getAsString();
                            if(!queryParamName.isEmpty()) {
                                queryParams.put(queryParamName, "");
                            }
                            state = State.END;
                        } else {
                            accumulator.put(current);
                        }
                        break;

                    case QUERY_PARAM_VALUE:
                        if(current == Util.AMPERSAND) {
                            if(!queryParamName.isEmpty()) {
                                queryParams.put(queryParamName, accumulator.getAsString());
                            }
                            accumulator.reset();
                            state = State.QUERY_PARAM_NAME;
                        } else if(current == Util.SP) {
                            if(!queryParamName.isEmpty()) {
                                queryParams.put(queryParamName, accumulator.getAsString());
                            }
                            state = State.END;
                        } else {
                            accumulator.put(current);
                        }
                        break;
                }
            }
            requestMessage.queryParameters = queryParams;
            return Status.DONE;
        }

        @Override
        public RequestMessage getResult() {
            return requestMessage;
        }

        private enum State {INITIAL, QUERY_PARAM_NAME, QUERY_PARAM_VALUE, END}
    }
}
