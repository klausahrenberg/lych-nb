package com.ka.lych.list;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author klausahrenberg
 * @param <K>
 * @param <V>
 */
public class LMap<K, V> extends ConcurrentHashMap<K, V> {

    public LMap() {
    }

    public LMap(int initialCapacity) {
        super(initialCapacity);
    }

    public static class LEntry<K, V>
            implements Entry<K, V> {

        private final K key;
        private V value;

        public LEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            this.value = value;
            return this.value;
        }

    }

    @SuppressWarnings("unchecked")
    public static <K, V> Entry<K, V> entry(K key, V value) {
        return new LEntry(key, value);
    }

    @SuppressWarnings("unchecked")
    public static <K, V> LMap<K, V> of(Entry<K, V>... entries) {
        var result = new LMap<K, V>();
        if (entries != null) {
            for (Entry<K, V> entry : entries) {
                if ((entry != null) && (entry.getValue() != null)) {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return result;
    }

    public static <K, V> LMap<K, V> of(Map<K, V> map) {
        Objects.requireNonNull(map, "Map cant be null");
        var result = new LMap<K, V>();
        map.forEach((k, v) -> {
            result.put(k, v);
        });
        return result;
    }

}
