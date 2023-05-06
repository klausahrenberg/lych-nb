package com.ka.lych.ui.observable;

import com.ka.lych.list.LList;
import com.ka.lych.observable.LObservable;
import com.ka.lych.util.ILConstants;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public interface ILHasItems<T> {
    
    public LObservable<LList<T>> items();
    
    public default String getItemsValueName() {
        return ILConstants.ITEMS;
    }
    
}
