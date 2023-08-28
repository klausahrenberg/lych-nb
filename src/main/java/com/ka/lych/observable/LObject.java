package com.ka.lych.observable;

import com.ka.lych.util.ILCloneable;
import java.util.EnumSet;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public class LObject<T> extends LObservable<T, LObject<T>> {

    public LObject() {
        this(null);
    }

    public LObject(T initialValue) {
        super(initialValue);
    }

    @Override
    public LObject<T> clone() throws CloneNotSupportedException {

        Object clonedObject = null;
        if ((this.isPresent()) && (this.get() instanceof Enum<?>)) {
            clonedObject = this.get();
        } else if ((this.isPresent()) && (this.get() instanceof EnumSet)) {
            clonedObject = ((EnumSet) this.get()).clone();
            /*} else if (source instanceof LInteger) {
                return LInteger.clone((LInteger) source);
            } else if (source instanceof LString) {
                return LString.clone((LString) source);*/
        } else if (this.isPresent()) {
            if (!(this.get() instanceof ILCloneable)) {
                throw new CloneNotSupportedException("Source object doesn't support cloning ('" + ILCloneable.class.getSimpleName() + "' not implemented): " + this.get().getClass());
            }
            clonedObject = ((ILCloneable) this.get()).clone();
        }
        return new LObject(clonedObject);
    }
    
    public static <T> LObject<T> of(T value) {
        return new LObject<>(value);
    }
    

}
