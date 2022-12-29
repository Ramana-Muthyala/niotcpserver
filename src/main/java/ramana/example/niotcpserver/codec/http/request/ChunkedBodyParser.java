package ramana.example.niotcpserver.codec.http.request;

import ramana.example.niotcpserver.codec.http.Util;
import ramana.example.niotcpserver.codec.parser.CompositeParser;
import ramana.example.niotcpserver.codec.parser.OneOrMore;
import ramana.example.niotcpserver.codec.parser.ZeroOrMore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChunkedBodyParser extends CompositeParser<byte[]> {
    Map<String, ArrayList<String>> headers;

    public ChunkedBodyParser(Map<String, ArrayList<String>> headers) {
        this.headers = headers;
        parsers.add(new OneOrMore<>(new ChunkParser()));
        parsers.add(new ZeroOrMore<>(new FieldLineParser()));
        parsers.add(Util.createCRLFParser());
    }

    @Override
    protected byte[] composeResult() {
        List<Field> trailers = (List<Field>) parsers.get(1).getResult();
        for (Field trailer: trailers) {
            if(!headers.containsKey(trailer.name)) headers.put(trailer.name, trailer.values);
        }
        headers.get(Util.REQ_HEADER_TRANSFER_ENCODING).remove(Util.REQ_HEADER_TRANSFER_ENCODING_CHUNKED);

        int tmp = 0;
        List<byte[]> list = (List<byte[]>) parsers.get(0).getResult();
        for (byte[] array: list) {
            tmp += array.length;
        }
        byte[] data = new byte[tmp];
        tmp = 0;
        for (byte[] array: list) {
            System.arraycopy(array, 0, data, tmp, array.length);
            tmp += array.length;
        }
        return data;
    }
}
