package com.ka.lych.observable;

import com.ka.lych.event.LActionEvent;
import com.ka.lych.event.LEvent;
import com.ka.lych.event.LEventHandler;
import com.ka.lych.list.LMap;
import com.ka.lych.list.LTYoso;
import com.ka.lych.util.ILConstants;
import com.ka.lych.util.ILHandler;
import com.ka.lych.util.ILSupportsOwner;
import com.ka.lych.annotation.Id;

/**
 *
 * @author klausahrenberg
 */
public class LObservables extends LTYoso<LObservables> 
        implements ILSupportsOwner {

    private final String DEFAULT_KEY = null;
        
    @Id
    private LString key;
    private LMap<String, LObservable> observables;
    private boolean hidden;
    private Object owner;
    
    @SuppressWarnings("unchecked")
    public LObservables(Object owner, String key) {
        this(owner, key, (ILHandler) null);
        this.hidden = false;        
    }
    
    @SuppressWarnings("unchecked")
    public LObservables(Object owner, String key, LObservable value) {
        this(owner, key, (ILHandler) null);
        if (value != null) {
            add(key, value);
        }    
    }
    
    public LObservables(Object owner, String key, ILHandler<LEvent> onAction) {
        super();
        setKey(key);
        this.owner = owner;
        this.hidden = true;
        if (onAction != null) {
            setOnAction(onAction);
        }
    }    

    public LString observableKey() {
        if (key == null) {
            key = new LString(DEFAULT_KEY);
        }
        return key;
    }

    public String getKey() {
        return key != null ? key.get() : DEFAULT_KEY;
    }

    public void setKey(String key) {
        observableKey().set(key);
    }

    public void add(String key, LObservable observable) {   
        if (observables == null) {
            observables = new LMap<>(8);
        }
        observables.put(key, observable);
    }

    @Override
    public LObservable observable(String fieldName) {
        LObservable result = null;
        try {
            result = super.observable(fieldName); 
        } catch (IllegalStateException ise) {}    
        if ((result == null) && (observables != null)) {
            result = observables.get(fieldName);
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    public void setOnAction(ILHandler<LEvent> onAction) {
        if (onAction != null) {
            add(ILConstants.ON_ACTION, new LEventHandler(onAction));
        } else if (observables != null) {
            observables.remove(ILConstants.ON_ACTION);
        }
    }
    
    @SuppressWarnings("unchecked")
    public LEventHandler<LActionEvent> getOnAction() {
        return (observables != null ? (LEventHandler) observables.get(ILConstants.ON_ACTION) : null);
    }

    public final void fireOnAction(LActionEvent actionEvent) {   
        LEventHandler<LActionEvent> onAction = getOnAction();
        if (onAction != null) {
            onAction.fireEvent(actionEvent);        
        } else {
            throw new UnsupportedOperationException("No action property defined");
        } 
    }

    public boolean isHidden() {
        return hidden;
    }

    @Override
    public Object getOwner() {
        return this.owner;
    }
    
    public boolean isTextAvailable() {
        return (observable(ILConstants.TEXT) != null);
    }
    
    public boolean isIconAvailable() {
        return (observable(ILConstants.ICON) != null);
    }

}
