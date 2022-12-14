package ramana.example.niotcpserver.codec.http.request;

import ramana.example.niotcpserver.codec.http.Util;
import ramana.example.niotcpserver.codec.parser.*;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;

public class ChunkParser extends AbstractParser<byte[]> {
    private final ByteSequence crlfParser;
    private final DelimiterStringParser chunkLenParser;
    private final DelimiterStringParser chunkExtParser;
    private State state = State.CHUNK_LEN;
    private int chunkLen;
    private FixedLengthParser chunkDataParser;

    public ChunkParser() {
        chunkLenParser = new DelimiterStringParser(Util.SP, Util.CR, Util.REQ_CHUNK_LEN_MAX_LEN, Util.REQ_CHUNK_LEN_MAX_LEN);
        chunkExtParser = new DelimiterStringParser(Util.CR, Util.CR, Util.REQ_CHUNK_EXT_MAX_LEN, Util.REQ_CHUNK_EXT_MAX_LEN);
        crlfParser = Util.createCRLFParser();
    }

    @Override
    public void parse(Allocator.Resource<ByteBuffer> data) throws ParseException, InternalException {
        if(status == Status.DONE) return;
        status = Status.IN_PROGRESS;

        ByteBuffer byteBuffer = data.get();
        while(byteBuffer.hasRemaining()) {
            switch (state) {
                case CHUNK_LEN:
                    parseChunkLength(data);
                    break;
                case CHUNK_EXT:
                    parseChunkExt(data);
                    break;
                case CRLF:
                    crlfParser.parse(data);
                    if(crlfParser.getStatus() == Status.DONE) {
                        crlfParser.reset();
                        state = State.CHUNK_DATA;
                    }
                    break;
                case CHUNK_DATA:
                    if(chunkDataParser == null) chunkDataParser = new FixedLengthParser(chunkLen);
                    chunkDataParser.parse(data);
                    if(chunkDataParser.getStatus() == Status.DONE) {
                        result = chunkDataParser.getResult();
                        state = State.LAST_CRLF;
                    }
                    break;
                case LAST_CRLF:
                    crlfParser.parse(data);
                    if(crlfParser.getStatus() == Status.DONE) {
                        status = Status.DONE;
                        return;
                    }
            }
        }
    }

    private void parseChunkExt(Allocator.Resource<ByteBuffer> data) throws ParseException, InternalException {
        boolean parsed = false;
        try {
            chunkExtParser.parse(data);
        } catch (DelimiterBreakPointParseException exception) {
            parsed = true;
        }
        if(parsed  ||  chunkExtParser.getStatus() == Status.DONE) {
            state = State.CRLF;
        }
    }

    private void parseChunkLength(Allocator.Resource<ByteBuffer> data) throws ParseException, InternalException {
        boolean parsed = false;
        try {
            chunkLenParser.parse(data);
        } catch (DelimiterBreakPointParseException exception) {
            parsed = true;
        }
        if(parsed  ||  chunkLenParser.getStatus() == Status.DONE) {
            try {
                chunkLen = Integer.parseInt(chunkLenParser.getResult(), 16);
            } catch (NumberFormatException exception) {
                throw new ParseException(exception);
            }
            if(chunkLen < 0) throw new ParseException();
            if(chunkLen == 0) {
                ByteBuffer byteBuffer = data.get();
                byteBuffer.position(byteBuffer.position() - 1);
                throw new ParseException();
            } else {
                state = State.CHUNK_EXT;
            }
        }
    }

    @Override
    public void reset() {
        chunkLen = 0;
        state = State.CHUNK_LEN;
        crlfParser.reset();
        chunkLenParser.reset();
        chunkExtParser.reset();
        chunkDataParser = null;
        super.reset();
    }

    private enum State {CHUNK_LEN, CHUNK_EXT, CRLF, CHUNK_DATA, LAST_CRLF}
}
