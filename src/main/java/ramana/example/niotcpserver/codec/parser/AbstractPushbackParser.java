package ramana.example.niotcpserver.codec.parser;

import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;

import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.LinkedList;

public abstract class AbstractPushbackParser<T> extends AbstractParser<T> {
    protected Deque<MarkWrapper> stack = new LinkedList<>();
    protected final Deque<Allocator.Resource<ByteBuffer>> dataDeque = new LinkedList<>();

    protected void pushBack() throws InternalException {
        MarkWrapper wrapper;
        while((wrapper = stack.pollLast()) != null) {
            wrapper.reset();
            dataDeque.offerFirst(wrapper.resource);
        }
    }

    protected void pushBack(Allocator.Resource<ByteBuffer> pushBackPoint) throws InternalException {
        MarkWrapper wrapper;
        while((wrapper = stack.pollLast()) != null) {
            if(wrapper.resource == pushBackPoint) {
                dataDeque.offerFirst(wrapper.resource); // do not reset pushBackPoint
                return;
            }
            wrapper.reset();
            dataDeque.offerFirst(wrapper.resource);
        }
    }

    @Override
    public void reset() {
        stack.clear();
        dataDeque.clear();
        super.reset();
    }

    protected static class MarkWrapper {
        private int position;
        private final Allocator.Resource<ByteBuffer> resource;

        public MarkWrapper(Allocator.Resource<ByteBuffer> resource, int position) {
            this.resource = resource;
            this.position = position;
        }

        private void reset() throws InternalException {
            resource.get().position(position);
        }

        public void mark(int position) {
            this.position = position;
        }
    }
}
