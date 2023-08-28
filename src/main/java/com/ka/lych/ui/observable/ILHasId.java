package com.ka.lych.ui.observable;

import com.ka.lych.observable.LString;

/**
 *
 * @author klausahrenberg
 */
public interface ILHasId<BC> {                
    
    public LString id();
       
    default public BC id(String id) {
        id().set(id);
        return (BC) this;
    }
    
}
