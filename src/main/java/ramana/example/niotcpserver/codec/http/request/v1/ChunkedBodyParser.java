package ramana.example.niotcpserver.codec.http.request.v1;

import ramana.example.niotcpserver.codec.http.Util;
import ramana.example.niotcpserver.codec.parser.Accumulator;
import ramana.example.niotcpserver.codec.parser.ParseException;
import ramana.example.niotcpserver.codec.parser.v1.*;
import ramana.example.niotcpserver.types.InternalException;

import java.util.ArrayList;

public class ChunkedBodyParser extends AbstractStateParser {
    private static final byte[] chunkLengthDelimiters = new byte[] { Util.SEMI_COLON, Util.SP, Util.CR };
    private final StringAccumulatorParser chunkLengthParser = new StringAccumulatorParser(chunkLengthDelimiters, Util.REQ_CHUNK_LEN_MAX_LEN, Util.REQ_CHUNK_LEN_MAX_LEN);
    private final SkipParser chunkExtParser = new SkipParser(Util.CR, Util.CR, Util.REQ_CHUNK_EXT_MAX_LEN);
    private final ByteSequence crlf = new ByteSequence(Util.CRLF);
    private final RequestMessage requestMessage;
    private State state = State.FIRST_CHUNK_LENGTH;
    private int chunkLength;
    private Accumulator accumulator;
    private int accumulatorIndex;
    private final ArrayList<byte[]> chunks = new ArrayList<>();

    public ChunkedBodyParser(RequestMessage requestMessage) {
        this.requestMessage = requestMessage;
    }

    @Override
    public void parse(Buffer buffer) throws ParseException, InternalException {
        while (state != State.END) {
            if(!buffer.hasRemaining()) return;

            switch (state) {
                case FIRST_CHUNK_LENGTH:
                    parseChunkLength(buffer, true);
                    break;

                case CHUNK_LENGTH:
                    parseChunkLength(buffer, false);
                    break;

                case CHUNK_EXT:
                    parseChunkExt(buffer);
                    break;

                case CHUNK_LENGTH_LF:
                    if(buffer.read() != Util.LF) throw new ParseException();
                    state = State.CHUNK_BODY;
                    break;

                case CHUNK_BODY:
                    parseChunkBody(buffer);
                    break;

                case CHUNK_BODY_CRLF:
                    ByteParser.Status status = crlf.parse(buffer);
                    if(status == ByteParser.Status.DONE) {
                        state = State.CHUNK_LENGTH;
                        crlf.reset();
                    } else if(status == ByteParser.Status.FAIL) throw new ParseException();
                    break;

                case LAST_CHUNK:
                    if(chunkLengthParser.getLastRead() != Util.CR) throw new ParseException();
                    if(buffer.read() != Util.LF) throw new ParseException();
                    state = State.END;
                    break;
            }
        }

        int capacity = 0;
        for (byte[] chunk : chunks) {
            capacity += chunk.length;
        }
        byte[] body = new byte[capacity];
        int index = 0;
        for (byte[] src : chunks) {
            System.arraycopy(src, 0, body, index, src.length);
            index += src.length;
        }
        requestMessage.body = body;

        done = true;
        next = new TrailerParser(requestMessage);
    }

    private void parseChunkBody(Buffer buffer) throws InternalException {
        if(accumulator == null) accumulator = new Accumulator(chunkLength);
        while(buffer.hasRemaining()) {
            byte tmp = buffer.read();
            accumulator.put(tmp);
            accumulatorIndex++;
            if(accumulatorIndex == chunkLength) {
                chunks.add(accumulator.getInternalByteArray());
                accumulator = null;
                accumulatorIndex = 0;
                state = State.CHUNK_BODY_CRLF;
                return;
            }
        }
    }

    private void parseChunkExt(Buffer buffer) throws InternalException, ParseException {
        ByteParser.Status status = chunkExtParser.parse(buffer);
        if(status == ByteParser.Status.DONE) {
            state = State.CHUNK_LENGTH_LF;
            chunkExtParser.reset();
        } else if(status == ByteParser.Status.FAIL) throw new ParseException();
    }

    private void parseChunkLength(Buffer buffer, boolean firstChunk) throws InternalException, ParseException {
        ByteParser.Status status = chunkLengthParser.parse(buffer);
        if(status == ByteParser.Status.DONE) {
            chunkLength = parseChunkLength(chunkLengthParser.getResult());
            if(chunkLength == 0) {
                if(firstChunk) throw new ParseException();
                else {
                    state = State.LAST_CHUNK;
                }
            } else {
                if(chunkLengthParser.getLastRead() == Util.CR) {
                    state = State.CHUNK_LENGTH_LF;
                } else {
                    state = State.CHUNK_EXT;
                }
                chunkLengthParser.reset();
            }
        } else if(status == ByteParser.Status.FAIL) throw new ParseException();
    }

    private int parseChunkLength(String input) throws ParseException {
        try {
            int length = Integer.parseInt(input, 16);
            if(length < 0) throw new ParseException();
            return length;
        } catch (NumberFormatException exception) {
            throw new ParseException(exception);
        }
    }

    private enum State {FIRST_CHUNK_LENGTH, CHUNK_LENGTH, CHUNK_EXT, CHUNK_LENGTH_LF, CHUNK_BODY, CHUNK_BODY_CRLF, LAST_CHUNK, END }
}
