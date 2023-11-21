package com.ka.lych.ui.observable;

import com.ka.lych.observable.LString;

/**
 *
 * @author klausahrenberg
 * @param <BC>
 */
public interface ILHasText<BC> {
    
    public LString text();
    
    default public BC text(String text) {
        text().set(text);
        return (BC) this;
    }
    
}
