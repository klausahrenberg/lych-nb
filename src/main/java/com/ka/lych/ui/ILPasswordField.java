package com.ka.lych.ui;

import com.ka.lych.event.LActionEvent;
import com.ka.lych.event.LErrorEvent;
import com.ka.lych.util.LParseException;
import com.ka.lych.util.ILHandler;
import com.ka.lych.ui.observable.ILHasTitle;

/**
 *
 * @author klausahrenberg
 */
public interface ILPasswordField extends ILHasTitle, ILSupportsObservables<String> {
    
    public void setOnChange(ILHandler<LActionEvent> onChange);  
    
    public void setOnError(ILHandler<LErrorEvent<LParseException>> onError);
    
}
