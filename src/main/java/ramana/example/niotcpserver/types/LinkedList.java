package ramana.example.niotcpserver.types;

public class LinkedList<T> {
    public static class LinkedNode<T> {
        private LinkedNode(T value) {
            this.value = value;
        }

        public T value;
        private boolean unlinked;
        public LinkedNode<T> next;
        public LinkedNode<T> prev;
    }

    public LinkedNode<T> head;
    public LinkedNode<T> tail;

    public void addFirst(T element) {
        if(head == null) {
            add(element);
            return;
        }
        LinkedNode<T> node = new LinkedNode<>(element);
        node.next = head;
        head.prev = node;
        head = node;
    }

    public void remove(LinkedNode<T> node) {
        if(node.unlinked) return;
        LinkedNode<T> prev = node.prev;
        LinkedNode<T> next = node.next;
        if(prev != null) prev.next = next;
        if(next != null) next.prev = prev;
        if(node == head) head = next;
        if(node == tail) tail = prev;
        node.unlinked = true;
    }

    public LinkedNode<T> add(T element) {
        LinkedNode<T> node = new LinkedNode<>(element);
        if(head == null) {
            head = tail = node;
        } else {
            tail.next = node;
            node.prev = tail;
            tail = node;
        }
        return node;
    }
}
