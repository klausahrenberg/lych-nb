package com.ka.lych.list;

import com.ka.lych.annotation.Id;
import com.ka.lych.observable.LString;
import com.ka.lych.util.ILConstants;
import com.ka.lych.util.LLog;
import com.ka.lych.util.LObjects;
import com.ka.lych.util.LReflections;
import com.ka.lych.util.LReflections.LFields;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

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

    public LJournal(LList<V> list) {
        this(list, Id.class);
    }
    
    public LJournal(LList<V> list, Class hashIndex) {
        _list = list;
        _hashIndex = hashIndex;
        _list.addListener(change -> {
            switch (change.type()) {
                case CHANGED -> {}
                case ADDED -> _add(change.item());
                case REMOVED -> {}
            }
        });
        _list.forEach(item -> _add(item));
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
        LJournalItem<V> ji = _slots[slot];
        while (ji != null) {
            if ((ji.getValue() != null) && (ji.getKey().equals(key))) {
                return true;
            } else {
                ji = ji.next();
            }
        }
        return false;        
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

    @Override
    public V put(String key, V item) {
        throw new UnsupportedOperationException("Method 'put' not supported.");
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> map) {
        throw new UnsupportedOperationException("Method 'putAll' not supported.");
    }

    @Override
    public V remove(Object item) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
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
    
    protected void _add(V item) {
        if (_fields == null) {
            _fields = LReflections.getFieldsOfInstance(item, null, _hashIndex);
            LLog.test(this, "hash fields are: %s", _fields.toString());
        }
        var key = _key(item);
        var slot = _slot(key);
        LLog.test(this, "slot is %s / key is '%s' / item: %s", slot, key, item);
        if (this.containsKey(key)) {
            //throw new LDoubleHashKeyException(this, "Key " + (key != null ? "'" + key + "'" : "null") + " already exists. (slot: " + slot + ")");
        }
        
        LJournalItem<V> ji = new LJournalItem<>(key, item);
        ji.next(_slots[slot]);
        _slots[slot] = ji;
    }

    protected void _removeHashKey(V item) {
        
    }
    
    protected String _key(V item) {
        LObjects.requireNonNull(_fields, "Fields can't be null.");
        if (_fields.size() == 0) {
            throw new IllegalArgumentException("Record has no IDs. (missing annotation @ID ?): class '" + item.getClass().getName() + "': " + item);
        }
        Object[] values = new Object[_fields.size()];
        for (int i = 0; i < _fields.size(); i++) {
            values[i] = _fields.get(i).value(item);
            LLog.test(this, "hashKey is %s of key: %s", values[i].hashCode(), values[i]);
        }                
        return LString.concatWithSpacer(ILConstants.DOT, ILConstants.NULL_VALUE, values);        
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
