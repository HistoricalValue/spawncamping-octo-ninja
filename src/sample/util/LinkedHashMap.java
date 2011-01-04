package sample.util;

public class LinkedHashMap<K, V> extends java.util.LinkedHashMap<K, V> {

    private static final long serialVersionUID = 235;

    public LinkedHashMap (int initialCapacity, float loadFactor, boolean accessOrder) {
        super(initialCapacity, loadFactor, accessOrder);
    }

    public LinkedHashMap (int initialCapacity) {
        super(initialCapacity);
    }

    public LinkedHashMap (int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }


    @Override
    public V put (final K key, final V value) {
        if (value == null)
            throw new NullPointerException();
        return super.put(key, value);
    }
}
