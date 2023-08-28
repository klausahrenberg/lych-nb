package com.ka.lych.ui.observable;

import com.ka.lych.observable.LBoolean;

/**
 *
 * @author klausahrenberg
 * @param <BC>
 */
public interface ILHasVisibility<BC> {                
    
    public LBoolean visible();
    
    default public BC visible(boolean visible) {
        visible().set(visible);
        return (BC) this;
    }
    
    default public boolean isVisible() {        
        return (visible().isPresent() ? visible().get() : true);
    }
        
}
