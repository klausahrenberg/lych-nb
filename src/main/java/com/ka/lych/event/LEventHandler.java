package com.ka.lych.event;

import com.ka.lych.list.LList;
import com.ka.lych.observable.ILChangeListener;
import com.ka.lych.observable.ILObservable;
import com.ka.lych.observable.LObservable;
import com.ka.lych.util.ILHandler;
import java.util.function.Function;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public class LEventHandler<T extends LEvent> extends LObservable<ILHandler<T>, LEventHandler> {

    private boolean supportMultipleListeners;
    private LList<ILHandler<T>> additionalListeners;

    private final ILChangeListener<ILHandler<T>, LEventHandler> valueListener = change -> {
        if ((change.newValue() != get()) && (supportMultipleListeners)) {
            if (change.newValue() == null) {                
                throw new IllegalArgumentException("Event properties with multiple listeners can't set to null. Use function remove(EventHandler<T>) instead.");
            }
            if (get() != null) {
                add(get());
            }
        }
    };
    
    public LEventHandler() {
        this(null);
    }
    
    public LEventHandler(ILHandler<T> initialValue) {
        super(initialValue);
        supportMultipleListeners = true;
        this.addListener(valueListener);
    }

    public void add(ILHandler<T> newValue) {
        if (supportMultipleListeners) {
            if (additionalListeners == null) {
                additionalListeners = new LList<>();
            }
            additionalListeners.add(newValue);
            _fireChangedEvent(new LObservableChangeEvent(this, null, null));
        } else {
            throw new IllegalStateException("Multiple listeners are not allowed.");
        }
    }

    public void remove(ILHandler<T> value) {
        if ((supportMultipleListeners) && (additionalListeners != null)) {
            additionalListeners.remove(value);
        }
        if (get() == value) {            
            super.set(null);            
        }
    }

    @SuppressWarnings("unchecked")
    public void fireEvent(T event) {
        if (this.isBoundInAnyWay()) {
            this.getBoundedObservables().forEach(observable -> ((LEventHandler) observable).fireEvent(event));            
        } else {
            if ((supportMultipleListeners) && (additionalListeners != null)) {
                additionalListeners.forEachIf(handler -> handler != get(), handler -> handler.handle(event));
            }
            if (this.get() != null) {
                this.get().handle(event);   
            }
        }
    }

    @Override
    public <B> void bind(Function<B, ILHandler<T>> converter, LObservable<B, ILObservable> observable) {
        if (observable == null) {
            throw new NullPointerException("Cannot bind to null");
        }
        if (converter != null) {
            throw new UnsupportedOperationException("Conversion of events not supported yet.");
        }
        if (!(observable instanceof LEventHandler)) {
            throw new IllegalArgumentException("Can only bind to " + LEventHandler.class.getName());
        }   
        super.bind(converter, observable);   
    }

    @Override
    public LEventHandler clone() throws CloneNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
