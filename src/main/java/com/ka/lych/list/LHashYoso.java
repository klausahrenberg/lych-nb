package com.ka.lych.list;

import com.ka.lych.observable.LObservable;

/**
 *
 * @author klausahrenberg
 * @param <K>
 */
public class LHashYoso<K> extends LYoso
        implements ILHashYoso<K> {

    private LObservable<K> hashKey;

    public LHashYoso() {
        this(null);
    }

    public LHashYoso(K hashKey) {
        setHashKey(hashKey);
    }

    @Override
    public LObservable<K> hashKey() {
        if (hashKey == null) {
            hashKey = new LObservable<>();
        }
        return hashKey;
    }

    @Override
    public K getHashKey() {
        return hashKey != null ? hashKey.get() : null;
    }

    @Override
    public final boolean setHashKey(K hashKey) {
        return hashKey().set(hashKey);
    }

}
