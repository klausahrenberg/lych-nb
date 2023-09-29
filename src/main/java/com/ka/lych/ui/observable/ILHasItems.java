package com.ka.lych.ui.observable;

import com.ka.lych.list.LList;
import com.ka.lych.observable.LObject;
import com.ka.lych.util.ILConstants;

/**
 *
 * @author klausahrenberg
 * @param <T>
 * @param <BC>
 */
public interface ILHasItems<T, BC> {
    
    public LObject<LList<T>> items();
    
    default public BC items(LList<T> items) {
        items().set(items);
        return (BC) this;
    }
    
    public default String getItemsValueName() {
        return ILConstants.ITEMS;
    }
    
}
