package com.ka.lych.ui;

import com.ka.lych.event.LActionEvent;
import com.ka.lych.event.LErrorEvent;
import com.ka.lych.exception.LParseException;
import com.ka.lych.ui.observable.ILHasEnabled;
import com.ka.lych.ui.observable.ILHasId;
import com.ka.lych.util.ILHandler;
import com.ka.lych.ui.observable.ILHasVisibility;

/**
 *
 * @author klausahrenberg
 */
public interface ILPasswordField<BC> extends ILHasId<BC>, ILHasVisibility<BC>, ILHasEnabled<BC> {
    
    public void setOnChange(ILHandler<LActionEvent> onChange);  
    
    public void setOnError(ILHandler<LErrorEvent<LParseException>> onError);
    
}
