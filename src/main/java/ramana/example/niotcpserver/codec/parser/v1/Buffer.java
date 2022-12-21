package ramana.example.niotcpserver.codec.parser.v1;

import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/*
* Usage scenarios: (mark() should follow reset() or / and release())
*   hasRemaining() must be invoked before read() failing which the buffer will be corrupted.
*   1. hasRemaining(), read()
*   2. mark(), hasRemaining(), read(), release()
*   3. mark(), hasRemaining(), read(), [reset(), hasRemaining(), read()]*, release()
* */
public class Buffer {
    private ArrayList<byte[]> data = new ArrayList<>();
    private Allocator.Resource<ByteBuffer> resource;
    private boolean mark;
    private int dataIndex;
    private int byteIndex;

    public void mark() throws InternalException {
        ByteBuffer byteBuffer = resource.get();
        if(byteBuffer.hasRemaining()) {
            byte[] tmp = new byte[byteBuffer.limit() - byteBuffer.position()];
            for (int i = 0; i < tmp.length; i++) {
                tmp[i] = byteBuffer.get();
            }
            data.add(tmp);
        }
        mark = true;
        resource.release();
        resource = null;
    }

    public void write(Allocator.Resource<ByteBuffer> resource) throws InternalException {
        this.resource = resource;
        if(mark) mark();
    }

    public boolean hasRemaining() throws InternalException {
        if(dataIndex < data.size()  &&  byteIndex == data.get(dataIndex).length) {
            byteIndex = 0;
            dataIndex++;
        }
        if(dataIndex != 0  &&  dataIndex == data.size()  &&  !mark) {
            data.clear();
            dataIndex = 0;
        }
        if(resource != null  &&  !resource.get().hasRemaining()) {
            resource.release();
            resource = null;
        }
        return dataIndex != data.size()  ||  resource != null;
    }

    public byte read() throws InternalException {
        // hasRemaining() must be invoked before read() failing which the buffer will be corrupted.
        // additional checks are not done in read() to improve performance.
        // onus is on the user.
        if(dataIndex < data.size()) return data.get(dataIndex)[byteIndex++];
        return resource.get().get();
    }

    public void reset() {
        dataIndex = 0;
        byteIndex = 0;
    }

    public void release() {
        ArrayList<byte[]> tmp = new ArrayList<>();
        while(dataIndex < data.size()) {
            tmp.add(data.get(dataIndex));
            dataIndex++;
        }
        dataIndex = 0;
        data = tmp;
        mark = false;
    }
}
