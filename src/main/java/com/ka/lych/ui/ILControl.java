package com.ka.lych.ui;

import com.ka.lych.ui.observable.ILHasId;
import com.ka.lych.ui.observable.ILHasVisibility;
import com.ka.lych.ui.observable.ILHasEnabled;

/**
 *
 * @author klausahrenberg
 */
public interface ILControl<BC> 
        extends ILHasId<BC>, ILHasVisibility<BC>, ILHasEnabled<BC> {            
    
}
