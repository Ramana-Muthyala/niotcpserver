package ramana.example.niotcpserver.types;

public class Pair <K, V>{
    public final K key;
    public final V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }
}
