package com.ka.lych.util;

import com.ka.lych.list.ILYosos;
import com.ka.lych.observable.LObservable;

/**
 *
 * @author klausahrenberg
 * @param <T>
 * @param <C>
 */
public interface ILSupportsChildrens<T, C extends ILYosos<T>> {
    
    public LObservable<C> childrens();
    
    public C getChildrens();    
    
    /**
     * Checks, if this object has some childrens. Even this function returns true, 
     * getChildrens() could still return null (in combination with llate loading childs).
     * To ensure the existance of childs, call loadChildrens() before getChildrens().
     * @return true, if object has childrens. 
     */
    public boolean hasChildrens();
    
    /**
     * Ensures the existence of childrens, if any.
     * @return true, if successful and more than one child
     */
    public boolean loadChildrens();
    
    default public boolean hasChildren(T children) {
        return ((hasChildrens()) && (getChildrens() != null) && (getChildrens().exists(children)));
    }
    
}
