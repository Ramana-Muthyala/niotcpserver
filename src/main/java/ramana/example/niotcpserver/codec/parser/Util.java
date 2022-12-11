package ramana.example.niotcpserver.codec.parser;

public class Util {
    public static final byte SP = ' ';
    public static final byte COLON = ':';
    public static final byte CR = '\r';
    public static OneByte createSpaceParser() {
        return new OneByte(SP);
    }

    public static EitherOfBytes createDigitParser() {
        return new EitherOfBytes(new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
    }

    public static OneByte createDotParser() {
        return new OneByte((byte)'.');
    }

    public static ByteSequence createCRLFParser() {
        return new ByteSequence(new byte[] {'\r', '\n'});
    }
}
