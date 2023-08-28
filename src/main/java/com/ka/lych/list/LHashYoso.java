package com.ka.lych.list;

import com.ka.lych.observable.LObject;

/**
 *
 * @author klausahrenberg
 * @param <K>
 */
public class LHashYoso<K> extends LYoso
        implements ILHashYoso<K> {

    private LObject<K> hashKey;

    public LHashYoso() {
        this(null);
    }

    public LHashYoso(K hashKey) {
        setHashKey(hashKey);
    }

    @Override
    public LObject<K> hashKey() {
        if (hashKey == null) {
            hashKey = new LObject<>();
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
