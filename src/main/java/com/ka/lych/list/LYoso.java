package com.ka.lych.list;

import com.ka.lych.observable.ILChangeListener;
import com.ka.lych.observable.ILObservable;
import com.ka.lych.observable.LBoolean;
import com.ka.lych.observable.LObservable;
import com.ka.lych.util.LReflections;
import com.ka.lych.util.LReflections.LField;
import com.ka.lych.util.LReflections.LMethod;
import com.ka.lych.xml.LXmlUtils;
import com.ka.lych.observable.ILObservables;
import com.ka.lych.util.LReflections.LFields;
import com.ka.lych.observable.ILValidator;
import java.util.Objects;
import com.ka.lych.annotation.Ignore;
import com.ka.lych.util.ILRegistration;

/**
 * LYoso represents an element (Yoso 요소) for a list. It's an implementation of
 * interface ILYoso
 *
 * @author klausahrenberg
 */
public abstract class LYoso
        implements ILYoso, ILObservables, ILObservable<LYoso> {

    private static LMap<Class, LFields> YOSO_FIELDS;
    private LList<LYosos> parents;
    private LList<ILChangeListener<LYoso>> changeListeners;
    private boolean updated;
    @Ignore
    private LBoolean changed;
    private LFields fields;
    protected LObservable[] oldKeyObjects;

    public LYoso() {
        evaluateFields(getClass(), this);
        this.updated = true;
        this.oldKeyObjects = null;
    }

    public static LFields evaluateFields(Class yosoClass) {
        return LYoso.evaluateFields(yosoClass, null);
    }

    public LFields getFields() {
        return fields;
    }

    @Override
    public int hashCode() {
        Object[] keys = new Object[fields.sizeKey() + 1];
        keys[0] = this.getClass();
        for (int i = 1; i <= fields.sizeKey(); i++) {
            keys[i] = observable(fields.get(i - 1)).get();
        }
        return Objects.hash(keys);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        for (int i = 1; i <= fields.sizeKey(); i++) {
            LField field = fields.get(i - 1);
            if (!Objects.equals(this.observable(field).get(), ((LYoso) obj).observable(field).get())) {
                return false;
            }
        }
        return true;
    }

    private static LFields evaluateFields(Class yosoClass, Object instance) {
        if (YOSO_FIELDS == null) {
            YOSO_FIELDS = new LMap<>(5);
        }
        LFields fields = YOSO_FIELDS.get(yosoClass);
        if (fields == null) {
            if (LYoso.class.isAssignableFrom(yosoClass)) {
                fields = LReflections.getFields(yosoClass, LObservable.class);
            } else {
                fields = LReflections.getFields(yosoClass, Object.class);
            }               
            YOSO_FIELDS.put(yosoClass, fields);
        }
        if ((instance != null) && (instance instanceof LYoso)) {
            ((LYoso) instance).fields = fields;
        }
        return fields;
    }
    
    @Override
    public LObservable observable(String fieldName) {
        return observable(fields.get(fieldName));
    }

    public LObservable observable(LField field) {
        return LReflections.observable(this, field);
    }
    
    public static boolean isYoso(Object yoso) {
        return ((yoso == null) || (yoso instanceof LYoso));
    }

    public String getFieldName(LObservable observable) {
        LField f = fields.get(observable, this);
        return (f != null ? f.name() : null);
    }

    @Override
    public void addParent(LYosos parent) {
        if (this.parents == null) {
            parents = LList.empty();
        }
        parents.add(parent);
    }

    @Override
    public void removeParent(LYosos parent) {
        if (this.parents != null) {
            parents.remove(parent);
        }
    }

    @Override
    public ILRegistration addListener(ILChangeListener<LYoso> changeListener) {
        if (this.changeListeners == null) {
            this.changeListeners = LList.empty();
        }
        this.changeListeners.add(changeListener);
        return () -> removeListener(changeListener);
    }

    @Override
    public void removeListener(ILChangeListener<LYoso> changeListener) {
        if (this.changeListeners != null) {
            this.changeListeners.remove(changeListener);
        }
    }

    @Override
    public ILRegistration addAcceptor(ILValidator<LYoso> valueAcceptor) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeAcceptor(ILValidator<LYoso> valueAcceptor) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isUpdated() {
        return updated;
    }

    @Override
    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public LBoolean observableChanged() {
        if (changed == null) {
            changed = new LBoolean(false);
        }
        return changed;
    }

    public final boolean isChanged() {
        return (changed != null ? changed.get() : false);
    }

    @Override
    public final void setChanged(boolean changed) {
        observableChanged().set(changed);
    }

    @SuppressWarnings("unchecked")
    protected void onChange() {
        setChanged(true);
        if (this.parents != null) {
            this.parents.forEach(yosos -> yosos.notifyChange(this));
        }
        if (this.changeListeners != null) {
            this.changeListeners.forEach(listener -> listener.changed(null));
        }
    }

    @Override
    public void onObservableChange(LObservable observable) {
        onChange();
    }

    public LObservable[] getOldKeyObjects() {
        return this.oldKeyObjects;
    }

    public void removeOldKey() {
        setOldKeyObjects(null);
    }

    protected void setOldKeyObjects(LObservable[] oldKeyObjects) {
        this.oldKeyObjects = oldKeyObjects;
    }

    @Override
    public String toString() {
        String result = LXmlUtils.fieldsToString(this, fields);
        return result;
    }

}
