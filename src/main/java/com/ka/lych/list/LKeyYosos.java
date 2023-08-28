package com.ka.lych.list;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Iterator;
import com.ka.lych.observable.ILChangeListener;
import com.ka.lych.observable.LObservable;
import com.ka.lych.util.ILConstants;
import com.ka.lych.util.LLog;
import com.ka.lych.observable.*;
import com.ka.lych.util.LTerm;
import com.ka.lych.util.LReflections;
import com.ka.lych.util.LReflections.LField;
import com.ka.lych.util.LReflections.LFields;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public class LKeyYosos<T extends LYoso> extends LYosos<T> {
    
    private final Class<T> dataClass;
    private LMap<String, T> keys;
    private LFields yosoFields;
    private LYoso parent;
    protected LTerm filter;

    private final ILValidator<Object, ILObservable> yosoKeyValidator = change -> {        
        //tbi
        String key = "";//createKey((T) change.getSource().getBean());
        printKeys();
        return (existsKey(key)
                ? new LValueException(this, "Key '" + key + "' already exists in list.")
                : null);
    };

    private final ILChangeListener<Object, ILObservable> yosoKeyListener = change -> {
        throw new UnsupportedOperationException("Not supported. tbi");
        /*T yoso = (T) change.getSource().getBean();
        //Update oldKeyObjects in yoso, if necessary
        if ((yoso.getOldKeyObjects() == null) && (change.getOldValue() != null)) {
            boolean complete = true;
            LObservable[] oldKeys = new LObservable[yosoFields.sizeKey()];
            for (int i = 0; (complete) && (i < yosoFields.sizeKey()); i++) {
                LField field = yosoFields.get(i);
                LObservable obs = yoso.observable(field);
                if (!field.isLinked()) {
                    complete = complete && (obs.isPresent());
                    //clone not linked fields
                    if (complete) {
                        try {
                            LObservable cloneObs = LObservable.clone(null, obs);
                            if (obs == change.getSource()) {
                                cloneObs.set(change.getOldValue());
                            }
                            oldKeys[i] = cloneObs;
                        } catch (CloneNotSupportedException cnse) {
                            LLog.error(this, "Storing oldKey failed: " + yoso, cnse);
                        }
                    }
                } else {
                    //linked fields are not cloned
                    //tbi
                    //complete = complete && (obs.isPresent()) && (((LYoso) obs.get()).getFields().getKeyCompleteness((LYoso) obs.get()) == LKeyCompleteness.KEY_COMPLETE);
                    oldKeys[i] = obs;
                }
            }
            if (complete) {                
                yoso.setOldKeyObjects(oldKeys);
            }
        }
        //Remove old key entry        
        keys.entrySet().removeIf(entry -> (entry.getValue() == yoso));
        //Create new key entry
        String key = createKey(yoso);
        keys.put(key, yoso);*/
    };

    public LKeyYosos() {
        this(null);
    }

    @SuppressWarnings("unchecked")
    public LKeyYosos(Class<T> dataClass) {
        super();        
        if (dataClass == null) {
            if (getClass() != LKeyYosos.class) {
                this.dataClass = LReflections.getParameterClass(getClass(), 0);
            } else {
                this.dataClass = null;
                //throw new IllegalArgumentException("Dataclass can't be null. Please specify in constructor");
            }
        } else {
            this.dataClass = dataClass;
        }
        this.keys = new LMap<>();
        this.yosoFields = null;
        filter = null;
    }

    public Class<T> getDataClass() {
        return dataClass;
    }

    public LYoso getParent() {
        return parent;
    }

    public void setParent(LYoso parent) {
        this.parent = parent;
    }

    public void printKeys() {
        keys.forEach((String k, T value) -> LLog.debug(this, "key '" + k + "' / value: " + value));
    }

    @Override
    public void add(int index, T yoso) {
        if (yoso == null) {
            throw new IllegalArgumentException("Can't add null elements.");
        }
        if (yosoFields == null) {
            yosoFields = yoso.getFields();
        }
        String key = createKey(yoso);
        if (existsKey(key)) {
            throw new IllegalArgumentException("Key '" + key + "' already exists in list.");
        }
        keys.put(key, yoso);
        addKeyListener(yoso);
        super.add(index, yoso);

    }

    @Override
    public T remove(int index) {
        T yoso = this.get(index);
        //Remove old key entry
        keys.entrySet().removeIf(entry -> (entry.getValue() == yoso));
        removeKeyListener(yoso);
        return super.remove(index);
    }

    public T get(Object... keys) {
        if (this.isEmpty()) {
            return null;
        }
        return this.keys.get(createKey(yosoFields, keys));
    }

    protected boolean existsKey(String key) {
        return keys.containsKey(key);
    }

    @SuppressWarnings("unchecked")
    protected void addKeyListener(T yoso) {
        for (int i = 0; i < yoso.getFields().sizeKey(); i++) {
            LObservable observable = yoso.observable(yoso.getFields().get(i));
            observable.addAcceptor(yosoKeyValidator);
            observable.addListener(yosoKeyListener);
        }
    }

    @SuppressWarnings("unchecked")
    protected void removeKeyListener(T yoso) {
        for (int i = 0; i < yoso.getFields().sizeKey(); i++) {
            LObservable observable = yoso.observable(yoso.getFields().get(i));
            observable.removeAcceptor(yosoKeyValidator);
            observable.removeListener(yosoKeyListener);
        }
    }

    public void setUpdatedAllYosos(boolean updated) {
        this.setUpdatedAllItems(null, updated);
    }

    public void setUpdatedAllItems(Object owner, boolean updated) {
        Iterator<T> itv = iterator();
        while (itv.hasNext()) {
            T item = itv.next();
            //if (((owner == null) && (item.owner == null)) || (item.owner == owner)) {
            item.setUpdated(false);
            //}
        }
        //doSomethingBeforeRequeryStarts();
    }

    @Override
    public void removeNotUpdatedYosos() {
        this.removeNotUpdatedItems(null);
    }

    public void removeNotUpdatedItems(Object owner) {
        if (removeNotUpdatedYosos) {
            for (int i = size() - 1; i >= 0; i--) {
                T item = get(i);
                //if (((owner == null) && (item.owner == null)) || (item.owner == owner)) {
                if (!item.isUpdated()) {
                    remove(i);
                }
                //}
            }
        }
        //doSomethingBeforeRequeryFinished();
    }

    public LTerm getFilter() {
        return filter;
    }

    public void setFilter(LTerm datasFilter) {
        this.filter = datasFilter;
    }

    @SuppressWarnings("unchecked")
    public T getOrAdd(Object... keys) {
        T yoso = get(keys);
        if (yoso == null) {
            yoso = (T) LReflections.newInstance(dataClass);
            LFields fields = LYoso.evaluateFields(dataClass);
            for (int i = 0; i < fields.sizeKey(); i++) {
                LField field = fields.get(i);
                yoso.observable(field).set(keys[i]);
            }
            this.add(yoso);
        }
        return yoso;
    }

    protected static String createKey(LYoso yoso) {
        if (yoso.getFields().sizeKey() == 0) {
            throw new IllegalArgumentException("Yoso has no IDs. (missing annotation @ID ?): class '" + yoso.getClass().getName() + "': " + yoso);
        }
        Object[] obs = new Object[yoso.getFields().sizeKey()];
        for (int i = 0; i < yoso.getFields().sizeKey(); i++) {
            obs[i] = yoso.observable(yoso.getFields().get(i));
        }
        return createKey(yoso.getFields(), obs);
    }

    @SuppressWarnings("unchecked")
    protected static String createKey(LFields fields, Object... keys) {
        if ((keys != null) && (keys.length > 0)) {
            Object[] keyStrings = new Object[keys.length];
            for (int i = 0; i < keys.length; i++) {
                Object key = keys[i];
                if (key instanceof LObservable) {
                    LObservable obs = (LObservable) key;
                    if (obs.get() != null) {
                        if (!fields.get(i).requiredClass().requiredClass().isAssignableFrom(obs.get().getClass())) {
                            throw new IllegalArgumentException("Observable object '" + obs.get() + "' has not the right parameter class '" + obs.get().getClass().getName() + "' is not the right type for key. Required class type: '" + fields.get(i).requiredClass().requiredClass().getName() + "'");
                        }
                        if (obs.get() instanceof LYoso) {
                            String yosoKey = createKey((LYoso) obs.get());
                            keyStrings[i] = yosoKey;
                        } else {
                            keyStrings[i] = obs.toParseableString();
                        }
                    } else {
                        keyStrings[i] = ILConstants.NULL_VALUE;
                    }
                } else if (key != null) {
                    if (!fields.get(i).requiredClass().requiredClass().isAssignableFrom(key.getClass())) {
                        throw new IllegalArgumentException("Key object '" + key + "' has not the right parameter class '" + key.getClass().getName() + "' is not the right type for key. Required class type: '" + fields.get(i).requiredClass().requiredClass().getName() + "'");
                    }
                    if (key instanceof String) {
                        keyStrings[i] = (String) key;
                    } else if (key instanceof Double) {
                        keyStrings[i] = LDouble.toParseableString((Double) key);
                    } else if (key instanceof Integer) {
                        keyStrings[i] = LInteger.toParseableString((Integer) key);
                    } else if (key instanceof Boolean) {
                        keyStrings[i] = LBoolean.toParseableString((Boolean) key);
                    } else if (key instanceof LocalDate) {
                        keyStrings[i] = LDate.toParseableString((LocalDate) key);
                    } else if (key instanceof LocalDateTime) {
                        keyStrings[i] = LDatetime.toParseableString((LocalDateTime) key);
                    } else if (key != null) {
                        if (key instanceof LYoso) {
                            String yosoKey = createKey((LYoso) key);
                            keyStrings[i] = yosoKey;
                        } else {
                            keyStrings[i] = key.toString();
                        }
                    }
                } else {
                    keyStrings[i] = ILConstants.NULL_VALUE;
                }
            }
            return LString.concatWithSpacer(ILConstants.DOT, ILConstants.NULL_VALUE, keyStrings);
        } else {
            throw new IllegalArgumentException("Objects are null or empty");
        }
    }

}
