package com.ka.lych.list;

import com.ka.lych.annotation.Id;
import com.ka.lych.exception.LDoubleHashKeyException;
import com.ka.lych.exception.LException;
import com.ka.lych.exception.LItemNotExistsException;
import com.ka.lych.exception.LUnchecked;
import com.ka.lych.exception.LValueException;
import com.ka.lych.observable.ILChangeListener;
import com.ka.lych.observable.ILObservable;
import com.ka.lych.observable.ILValidator;
import com.ka.lych.observable.LObservable;
import com.ka.lych.observable.LString;
import com.ka.lych.util.ILConstants;
import com.ka.lych.util.LLog;
import com.ka.lych.util.LObjects;
import com.ka.lych.util.LReflections;
import com.ka.lych.util.LReflections.LFields;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author klausahrenberg
 */
public class LJournal<V>
        implements Map<String, V> {

    private static final int NUMBER_OF_SLOTS = 128;

    protected final LList<V> _list;
    protected final Class _hashIndex;
    protected LFields _fields;
    private final LJournalItem<V>[] _slots = new LJournalItem[NUMBER_OF_SLOTS];
    private final ILValidator<Object, ILObservable> _observableAcceptor = change -> {
        var newKey = _key((V) change.source().owner(), false, null, null);
        return (containsKey(newKey)
                ? new LValueException("Key '%s' already exists.", newKey)
                : null);
    };
    private final ILChangeListener<Object, ILObservable> _observableListener = change -> {
        LLog.test("time for change %s", change.source().owner());
        var oldKey = _key((V) change.source().owner(), false, change.source(), change.oldValue());
        if (_removeKey(oldKey)) {
            var newKey = _key((V) change.source().owner(), false, null, null);
            //_addKey(newKey, (V) change.source().owner());
            if (!_addKey(newKey, (V) change.source().owner())) {
                throw new LUnchecked("Key " + (newKey != null ? "'" + newKey + "'" : "null") + " already exists.");
            }
        } 
    };

    public LJournal(LList<V> list) {
        this(list, Id.class);
    }

    public LJournal(LList<V> list, Class hashIndex) {
        LObjects.requireNonNull(list);
        LObjects.requireNonNull(hashIndex);
        _list = list;
        _hashIndex = hashIndex;
        _list.forEach(LException.throwing(item -> _add(item)));
        _list.addListener(change -> {
            switch (change.type()) {
                case CHANGED -> {
                }
                case ADDED ->
                    _add(change.item());
                case REMOVED ->
                    _remove(change.item());
                default -> {
                }
            };
        });
    }

    @Override
    public int size() {
        return _list.size();
    }

    @Override
    public boolean isEmpty() {
        return _list.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        LObjects.requireNonNull(key, "Key can't be null");
        LObjects.requireClass(key, String.class, "Key must be a String: " + key.getClass());
        var slot = _slot((String) key);
        return (_containsKey((String) key, slot) > -1);
    }

    protected int _containsKey(String key, int slot) {
        int i = 0;
        LJournalItem<V> ji = _slots[slot];
        while (ji != null) {
            if ((ji.getValue() != null) && (ji.getKey().equals(key))) {
                return i;
            } else {
                ji = ji.next();
            }
            i++;
        }
        return -1;
    }

    @Override
    public boolean containsValue(Object item) {
        return containsKey(_key((V) item));
    }

    @Override
    public V get(Object key) {
        LObjects.requireNonNull(key, "Key can't be null");
        LObjects.requireClass(key, String.class, "Key must be a String: " + key.getClass());
        var slot = _slot((String) key);
        LJournalItem<V> ji = _slots[slot];
        while (ji != null) {
            if ((ji.getValue() != null) && (ji.getKey().equals(key))) {
                return ji.getValue();
            } else {
                ji = ji.next();
            }
        }
        return null;
    }

    public LJournal<V> add(V item) {
        return this.put(item);
    }

    public LJournal<V> put(V item) {
        _list.add(item);
        return this;
    }

    @Override
    @Deprecated
    public V put(String key, V item) {
        throw new UnsupportedOperationException("Method 'put' not supported.");
    }

    @Override
    @Deprecated
    public void putAll(Map<? extends String, ? extends V> map) {
        throw new UnsupportedOperationException("Method 'putAll' not supported.");
    }

    @Override
    public V remove(Object item) {
        _list.remove(item);
        return (V) item;
    }

    @Override
    public void clear() {
        _list.clear();
    }

    @Override
    public Set<String> keySet() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Collection<V> values() {
        return _list;
    }

    @Override
    public Set<Entry<String, V>> entrySet() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    protected void _add(V item) throws LDoubleHashKeyException {
        if (_fields == null) {
            _fields = LReflections.getFieldsOfInstance(item, null, _hashIndex);
        }
        var key = _key(item, true, null, null);
        if (!_addKey(key, item)) {
            throw new LDoubleHashKeyException("Key '%s' already exists.", key);
        }
        LLog.test("added (%s items): %s", this.size(), item);
    }
    
    protected boolean _addKey(String key, V item) {
        var slot = _slot(key);
        //LLog.test(this, "slot is %s / key is '%s' / item: %s", slot, key, item);
        if (_containsKey(key, slot) == -1) {
            LJournalItem<V> ji = new LJournalItem<>(key, item);
            ji.next(_slots[slot]);
            _slots[slot] = ji;
            return true;
        } else {
            return false;
        }
    }

    protected V _remove(V item) throws LItemNotExistsException {
        if (_fields != null) {
            var key = _key(item);
            if (!_removeKey(key)) {
                throw new LItemNotExistsException("Journal contains no key '%s'", key);
            } else {
                _removeListeners(item);
            }
        }
        return item;
    }

    protected boolean _removeKey(String key) {
        var slot = _slot(key);
        if (_containsKey(key, slot) > -1) {
            LJournalItem last = null;
            LJournalItem hi = _slots[slot];
            while (hi != null) {
                if (!hi.getKey().equals(key)) {
                    last = hi;
                    hi = hi.next();
                } else {
                    if (last == null) {
                        _slots[slot] = hi.next();
                    } else {
                        last.next(hi.next());
                    }
                    hi.setValue(null);
                    hi.next(null);
                    hi = null;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    protected String _key(V item) {
        return _key(item, false, null, null);
    }

    protected String _key(V item, boolean addListeners, LObservable oldObservable, Object oldValue) {
        LObjects.requireNonNull(_fields, "Fields can't be null.");
        if (_fields.size() == 0) {
            throw new IllegalArgumentException("Record has no IDs. (missing annotation @ID ?): class '" + item.getClass().getName() + "': " + item);
        }
        Object[] values = new Object[_fields.size()];
        for (int i = 0; i < _fields.size(); i++) {
            var field = _fields.get(i);

            if (field.isObservable()) {
                var obs = field.observable(item);
                values[i] = obs.get();
                if (addListeners) {
                    obs.owner(item);
                    obs.addAcceptor(_observableAcceptor);
                    obs.addListener(_observableListener);
                } else if (obs == oldObservable) {
                    values[i] = oldValue;
                }
            } else {
                values[i] = field.value(item);
            }

            LLog.test("hashKey is %s of key: %s", values[i].hashCode(), values[i]);
        }
        return LString.concatWithSpacer(ILConstants.DOT, ILConstants.NULL_VALUE, values);
    }
    
    protected void _removeListeners(V item) {        
        for (int i = 0; i < _fields.size(); i++) {
            var field = _fields.get(i);

            if (field.isObservable()) {
                var obs = field.observable(item);
                obs.owner(null);
                obs.removeAcceptor(_observableAcceptor);
                obs.removeListener(_observableListener);
            }
        }
    }

    protected int _slot(String key) {
        return LObjects.hash(key) % NUMBER_OF_SLOTS;
    }

    protected static class LJournalItem<V>
            implements Entry<String, V> {

        LJournalItem<V> _next;
        String _key;
        V _value;

        public LJournalItem(String key, V value) {
            _key = key;
            _value = value;
        }

        @Override
        public String getKey() {
            return _key;
        }

        @Override
        public V getValue() {
            return _value;
        }

        @Override
        public V setValue(V value) {
            var oldValue = _value;
            _value = value;
            return oldValue;
        }

        public LJournalItem<V> next() {
            return _next;
        }

        public void next(LJournalItem<V> next) {
            _next = next;
        }

    }

}
