package com.ka.lych.list;

/**
 *
 * @author klausahrenberg
 * @param <K>
 * @param <V>
 */
public interface ILHashYosos<K, V extends ILHashYoso> extends ILYosos<V> {

    public V get(K key);

    public V get(K key, boolean recursive);

    public boolean removeByKey(K key);

}
