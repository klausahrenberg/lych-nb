package com.ka.lych.observable;

import java.util.EnumSet;
import java.util.Objects;
import com.ka.lych.event.LObservableChangeEvent;
import com.ka.lych.util.ILCloneable;
import com.ka.lych.util.ILLocalizable;
import com.ka.lych.util.LParseException;
import com.ka.lych.xml.LXmlUtils;
import com.ka.lych.list.LList;
import com.ka.lych.util.ILLoadable;
import com.ka.lych.util.ILParseable;
import com.ka.lych.util.ILRegistration;
import com.ka.lych.util.LLoadingState;
import com.ka.lych.util.LLog;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public class LObservable<T> 
        implements ILObservable<T>, ILLoadable, ILParseable, ILLocalizable {

    private T value;
    private LList<ILChangeListener<T>> changeListeners;
    private LList<ILValidator<T>> changeAcceptors;
    private LList<LObservable<Object>> boundedObservables;    
    private Function<Object, T> boundedConverter;
    private boolean changed;
    private boolean notifyAllowed;
    private LValueException lastException;
    private Function<LObservable, Boolean> lateLoader;    
    protected LLoadingState loadingState;

    @SuppressWarnings("unchecked")
    protected final ILChangeListener<Object> boundedObservableListener = change -> { 
        if (this.isSingleBound()) {
            this.fireChangedEvent(new LObservableChangeEvent<>(this, null, (change != null && change.getOldValue() != null ? (T) (boundedConverter != null ? boundedConverter.apply(change.getOldValue()) : change.getOldValue()) : null)));
        } else {
            //Multiple bounds: compose new value with converter and set local value
            T newValue = boundedConverter.apply(boundedObservables);            
            set(newValue);            
        }    
    };

    private ILRegistration valueListener;        

    public LObservable() {
        this(null);
    }

    public LObservable(T initialValue) {
        super();
        this.boundedObservables = null;
        this.boundedConverter = null;
        this.notifyAllowed = true;
        set(initialValue);        
        this.changed = false;
        this.lastException = null;
        this.lateLoader = null;
        this.loadingState = LLoadingState.LOADED;
    }

    @SuppressWarnings("unchecked")
    public T get() {
        if (!isSingleBound()) {            
            return value;
        } else {
            return ((T) (boundedConverter != null ? boundedConverter.apply(getSingleBoundedObservable().get()) : getSingleBoundedObservable().get()));
        }    
    }
    
    public boolean isAbsent() {
        return (!isPresent());
    }
    
    public LObservable<T> ifAbsent(Consumer<? super T> consumer) {
        if (isAbsent()) {
            consumer.accept(get());
        }
        return this;
    }
    
    public boolean isPresent() {
        return (isSingleBound() ? getSingleBoundedObservable().isPresent() : value != null);        
    }
    
    public LObservable<T> ifPresent(Consumer<? super T> consumer) {
        if (isPresent()) {
            consumer.accept(get());
        }
        return this;
    }
    
    public T orElse(T other) {
        return (isPresent() ? get() : other);
    }
    
    public <U> Optional<U> map(Function<? super T,? extends U> mapper) {
        return ((mapper != null) && (isPresent()) ? Optional.of(mapper.apply(get())) : Optional.empty());        
    }
    
    /**
     * If a value is present, returns the value, otherwise throws
     * {@code NoSuchElementException}.
     *
     * @return the non-{@code null} value described by this {@code Optional}
     * @throws NoSuchElementException if no value is present
     * @since 10
     */
    public T orElseThrow() {
        if (get() == null) {
            throw new NoSuchElementException("No value present");
        }
        return get();
    }
    
    /**
     * If a value is present, returns the value, otherwise throws an exception
     * produced by the exception supplying function.
     * @param <X> Type of the exception to be thrown
     * @param exceptionSupplier the supplying function that produces an exception to be thrown
     * @return the value, if present
     * @throws X if no value is present
     * @throws NullPointerException if no value is present and the exception supplying function is {@code null}
     */
    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (get() != null) {
            return get();
        } else {
            throw exceptionSupplier.get();
        }
    }

    /** Can be used to override in extended classes and change the new value before set and comparing, 
     *  e.g. in class LUrl it's used to load a resource link, if value starts with @
     * 
     * @param newValue that is intended to set
     * @return changed value, default behaviour just throws the original value
     */    
    protected T preconfigureValue(T newValue) {
        return newValue;
    }
    
    @SuppressWarnings("unchecked")
    final public void setValue(T value) throws LValueException {        
        setValue(value, null);
    }
    
    @SuppressWarnings("unchecked")
    final public void setValue(T value, Object trigger) throws LValueException {        
        if (!isSingleBound()) {
            lastException = null;    
            value = preconfigureValue(value);
            if (!Objects.equals(get(), value)) {                
                T oldValue = this.value;
                this.value = value;
                LObservableChangeEvent<T> changeEvent = new LObservableChangeEvent<>(this, trigger, oldValue);
                if (changeAcceptors != null) {
                    changeAcceptors.forEachIf(validator -> (lastException == null), validator -> lastException = validator.accept(changeEvent));
                }
                if (lastException == null) {
                    if (valueListener != null) {
                        valueListener.remove();
                    }
                    if ((this.value != null) && (this.value instanceof ILObservable)) {                        
                        valueListener = ((ILObservable<T>) this.value).addListener(change -> this.fireChangedEvent(new LObservableChangeEvent<>(this, null, (change != null ? change.getOldValue() : null))));
                    }                    
                    this.fireChangedEvent(changeEvent);                    
                } else {
                    this.value = oldValue;
                }
            }
            if (lastException != null) {
                throw lastException;
            }
        } else {
            if (boundedConverter != null) {
                throw new LValueException(this, "Can't set a bounded observable, if converter is needed.");
            }
            getSingleBoundedObservable().set(value);
        }
    }
    
    /**
     * Sets the value without throwing Exception. Exception is printed. 
     * @param value 
     * @return false, if something went wrong. Call getLastException() for last failure 
     */
    final public boolean set(T value) {
        try {
            setValue(value);
            return true;
        } catch (LValueException lve) {
            LLog.error(this, lve.getMessage(), lve);
            return false;
        }
    }
    
    @SuppressWarnings("unchecked")
    public boolean isValueInstanceOf(Class cl) {
        return ((isAbsent()) || (cl.isAssignableFrom(get().getClass())));
    }

    public LValueException getLastException() {
        return (!isSingleBound() ? lastException : getSingleBoundedObservable().getLastException());
    }

    protected void fireChangedEvent(LObservableChangeEvent<T> changeEvent) {        
        if (notifyAllowed) { 
            if (changeListeners != null) {
                changeListeners.forEach(changeListener -> changeListener.changed(changeEvent));
            }
            changed = false;
        }
    }

    @Override
    public synchronized ILRegistration addListener(ILChangeListener<T> changeListener) {
        if (changeListeners == null) {
            changeListeners = LList.empty();
        }
        changeListeners.add(changeListener);
        return () -> removeListener(changeListener);
    }

    @Override
    public synchronized void removeListener(ILChangeListener<T> changeListener) {
        if (changeListeners != null) {
            changeListeners.remove(changeListener);
            if (changeListeners.isEmpty()) {
                changeListeners = null;
            }
        }
    }

    @Override
    public synchronized ILRegistration addAcceptor(ILValidator<T> changeAcceptor) {
        if (changeAcceptors == null) {
            changeAcceptors = LList.empty();
        }
        changeAcceptors.add(changeAcceptor); 
        return () -> removeAcceptor(changeAcceptor);
    }

    @Override
    public synchronized void removeAcceptor(ILValidator<T> changeAcceptor) {
        if (changeAcceptors != null) {
            changeAcceptors.remove(changeAcceptor);
            if (changeAcceptors.isEmpty()) {
                changeAcceptors = null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void parse(String value) throws LParseException {
        var v = get();
        if (v == null) {
            throw new IllegalStateException("Can't parse ObjectValue, if value is not setted yet.");
        } else if (v.getClass().isEnum()) {
            try {
                setValue((T) LXmlUtils.xmlStrToEnum(value, v.getClass()));
            } catch (LValueException lve) {
                throw new LParseException(this, lve.getMessage(), lve);
            }
        } else if (EnumSet.class.isAssignableFrom(v.getClass())) {
            throw new UnsupportedOperationException("EnumSets are not supported yet.");
        } else {
            throw new UnsupportedOperationException("Not supported yet: " + v);
        }
    }

    public void parseLocalized(String value) throws LParseException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toParseableString() {
        var v = get();
        return (v != null ? (v instanceof ILParseable ? ((ILParseable) v).toParseableString() : v.toString()) : null);
    }

    @Override
    public String toLocalizedString() {
        return (get() != null ? (get() instanceof ILLocalizable ? ((ILLocalizable) get()).toLocalizedString() : get().toString()) : null);
    }

    @SuppressWarnings("unchecked")
    public static LObservable clone(LObservable source) throws CloneNotSupportedException {
        if (source != null) {
            /*if (source.isBoundInAnyWay()) {
                throw new IllegalStateException("Bounded observable can't be cloned.");
            }*/
            Object clonedObject = null;             
            if ((source.get() != null) && (source.get() instanceof Enum<?>)) {
                clonedObject = source.get();
            } else if ((source.get() != null) && (source.get() instanceof EnumSet)) {                
                clonedObject = ((EnumSet) source.get()).clone();                
            } else if (source instanceof LInteger) {
                return LInteger.clone((LInteger) source);
            } else if (source instanceof LString) {
                return LString.clone((LString) source);
            } else if (source.get() != null) {
                if (!(source.get() instanceof ILCloneable)) {
                    throw new CloneNotSupportedException("Source object doesn't support cloning ('" + ILCloneable.class.getSimpleName() + "' not implemented): " + source.get().getClass());
                }
                clonedObject = ((ILCloneable) source.get()).clone();
            }
            return new LObservable(clonedObject);
        }
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.value);
    }

    @Override
    public boolean equals(Object obj) {      
        if (this == obj) {
            return true;
        }                
        if (obj == null) {
            return (this.value == null);
        }                
        if (obj instanceof LObservable) {
            obj = ((LObservable) obj).value;            
        }
        return Objects.equals(this.value, obj);
        /*if (getClass() != obj.getClass()) {
            return false;
        }
        final LObservable<?> other = (LObservable<?>) obj;
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;*/
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + value + "]";
    }

    @SuppressWarnings("unchecked")
    public void bind(LObservable<T> observable) {
        bind(null, (LObservable<Object>) observable);
    }
    
    public <B> void bind(Function<B, T> converter, LObservable<B> observable) {
        var observables = new LObservable[1];
        observables[0] = observable;
        this.bind(converter, observables);
    }
    
    @SuppressWarnings("unchecked")
    public <B> void bind(Function<?, T> converter, LObservable... observables) {
        if ((observables == null) || (observables.length == 0)) {
            throw new IllegalArgumentException("Cannot bind to null");
        }
        if ((converter == null) && (observables.length > 1)) {
            throw new IllegalStateException("Can't bind to multiple observables without a converter.");
        }
        /*if (changeAcceptors != null) {
            throw new IllegalStateException("Can't bind to another observable, if acceptors are present.");
        }*/
        if (lateLoader != null) {
            throw new IllegalStateException("Can't bind to a late loading observable to another one.");
        }
        //if (!observable.equals(boundedObservable)) {
            unbind();
            this.boundedConverter = (Function<Object, T>) converter;            
            boundedObservables = LList.empty();
            for (int i = 0; i < observables.length; i++) {
                if (observables[i] == null) {
                    throw new IllegalArgumentException("Can't add null observable: arrayIndex " + i);
                }
                boundedObservables.add((LObservable<Object>) observables[i]);            
                ((LObservable<Object>) observables[i]).addListener(boundedObservableListener);
            }
            if (this.isSingleBound()) {
                //at single bound notify listeners
                boundedObservableListener.changed(null);//new LObservableChangeEvent<>(this, null));
            } else {
                //at multiple bind, compose new local value
                T newValue = this.boundedConverter.apply(boundedObservables);
                set(newValue);                
            }
        //}
    }

    public void unbind() {
        if (boundedObservables != null) {
            if (isSingleBound()) {
                value = get();
            }
            boundedObservables.forEach(boundedObservable -> boundedObservable.removeListener(boundedObservableListener));
            boundedObservables.clear();
            boundedObservables = null;
            boundedConverter = null;
        }
    }

    public boolean isBoundInAnyWay() {
        return ((boundedObservables != null) && (boundedObservables.size() > 0));
    }
    
    public boolean isSingleBound() {
        return ((boundedObservables != null) && (boundedObservables.size() == 1));
    }

    protected LList<LObservable<Object>> getBoundedObservables() {
        return boundedObservables;
    }

    protected LObservable<Object> getSingleBoundedObservable() {
        return boundedObservables.get(0);
    }        

    @Override
    public boolean isLoadable() {
        return (lateLoader != null);
    }
    
    public void setLateLoader(Function<LObservable, Boolean> lateLoader) {
        if (isBoundInAnyWay()) {
            throw new IllegalStateException("Can't set late loading for a bounded observable.");
        }
        this.lateLoader = lateLoader;
        this.loadingState = (this.lateLoader != null ? LLoadingState.NOT_LOADED : LLoadingState.LOADED);        
    }

    @Override
    public LLoadingState getLoadingState() {
        return loadingState;
    }        

    @Override
    public void load() {      
        if ((this.isLoadable()) && (this.loadingState == LLoadingState.NOT_LOADED)) {
            //return LServices.execute((LService runnable, long now) -> {
                this.loadingState = LLoadingState.LOADING;
                this.loadingState = (this.lateLoader.apply(this) ? LLoadingState.LOADED : LLoadingState.NOT_LOADED);
            //});
        } /*else {
            return LWaiter.confirmation();
        }*/
    }

}
