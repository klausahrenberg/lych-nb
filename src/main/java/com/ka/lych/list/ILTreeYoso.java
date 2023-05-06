package com.ka.lych.list;

import com.ka.lych.util.ILSupportsChildrens;

/**
 *
 * @author klausahrenberg
 * @param <K>
 * @param <V>
 */
public interface ILTreeYoso<K, V extends ILTreeYoso> extends ILSupportsChildrens<V, ILYosos<V>>, ILHashYoso<K> {

}
