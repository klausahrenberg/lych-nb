package com.ka.lych.list;

import com.ka.lych.observable.LObject;

/**
 *
 * @author klausahrenberg
 * @param <K> hash key
 */
public interface ILHashYoso<K> extends ILYoso {

    public LObject<K> hashKey();

    public K getHashKey();

    public boolean setHashKey(K hashKey);

}
