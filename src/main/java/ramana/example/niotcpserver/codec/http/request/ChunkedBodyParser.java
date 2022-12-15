package ramana.example.niotcpserver.codec.http.request;

import ramana.example.niotcpserver.codec.http.Util;
import ramana.example.niotcpserver.codec.parser.CompositeParser;
import ramana.example.niotcpserver.codec.parser.OneOrMore;
import ramana.example.niotcpserver.codec.parser.ZeroOrMore;

import java.util.HashMap;
import java.util.List;

public class ChunkedBodyParser extends CompositeParser<byte[]> {
    private final List<Field> headers;

    public ChunkedBodyParser(List<Field> headers) {
        this.headers = headers;
        parsers.add(new OneOrMore<>(new ChunkParser()));
        parsers.add(new LastChunkParser());
        parsers.add(new ZeroOrMore<>(new FieldLineParser()));
        parsers.add(Util.createCRLFParser());
    }

    @Override
    protected byte[] composeResult() {
        List<Field> trailers = (List<Field>) parsers.get(2).getResult();
        if(trailers.size() > 0) {
            HashMap<String, Field> hashMap = new HashMap<>(Math.max(headers.size(), trailers.size()) * 2);
            for (Field field: trailers) {
                hashMap.put(field.name, field);
            }
            for (Field field: headers) {
                hashMap.put(field.name, field);
            }
            hashMap.get(Util.REQ_HEADER_TRANSFER_ENCODING).values.remove(Util.REQ_HEADER_TRANSFER_ENCODING_CHUNKED);
            headers.clear();
            headers.addAll(hashMap.values());
        }

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