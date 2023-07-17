package com.ka.lych.ui.observable;

import com.ka.lych.observable.LString;

/**
 *
 * @author klausahrenberg
 */
public interface ILHasTitle<BC> {                        
    
    public LString title();       
        
    @SuppressWarnings("unchecked")
    default public BC title(String title) {
        title().set(title);        
        return (BC) this;
    }
        
}
