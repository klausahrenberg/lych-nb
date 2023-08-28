package com.ka.lych.ui.observable;

import com.ka.lych.observable.LBoolean;

/**
 *
 * @author klausahrenberg
 * @param <BC>
 */
public interface ILHasEnabled<BC> {                
    
    public LBoolean enabled();    
    
    default public BC enabled(boolean enabled) {
        enabled().set(enabled);
        return (BC) this;
    }
    
    default public boolean isEnabled() {        
        return (enabled().isPresent() ? enabled().get() : true);
    }
    
}
