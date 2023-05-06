package com.ka.lych.ui;

import java.util.Iterator;

/**
 *
 * @author klausahrenberg
 * @param <C>
 */
public interface ILChildrensIterable<C> {
    
    public Iterator<C> getChildrensIterator();
    
}
