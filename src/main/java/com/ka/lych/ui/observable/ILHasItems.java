package com.ka.lych.ui.observable;

import com.ka.lych.list.LList;
import com.ka.lych.observable.LObject;
import com.ka.lych.util.ILConstants;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public interface ILHasItems<T> {
    
    public LObject<LList<T>> items();
    
    public default String getItemsValueName() {
        return ILConstants.ITEMS;
    }
    
}
