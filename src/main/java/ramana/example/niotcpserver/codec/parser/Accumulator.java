package ramana.example.niotcpserver.codec.parser;

public class Accumulator {
    private byte[] data;
    private int index;

    public Accumulator(int capacity) {
        data = new byte[capacity];
    }

    public void put(byte input) {
        if(index == data.length) {
            byte[] tmp = data;
            data = new byte[index << 1];
            System.arraycopy(tmp, 0, data, 0, index);
        }
        data[index] = input;
        index++;
    }

    public String get() {
        return new String(data, 0, index);
    }
}
