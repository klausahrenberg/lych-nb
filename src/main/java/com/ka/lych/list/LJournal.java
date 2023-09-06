package com.ka.lych.list;

import com.ka.lych.annotation.Id;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author klausahrenberg
 */
public class LJournal<K, V> 
        implements Map<K, V> {
    
    protected final LList<V> _list;
    protected final Class _hashIndex;

    public LJournal(LList<V> list) {
        this(list, Id.class);
    }
    
    public LJournal(LList<V> list, Class hashIndex) {
        _list = list;
        _hashIndex = hashIndex;
        _list.addListener(change -> {
            switch (change.type()) {
                case CHANGED -> {}
                case ADDED -> _addHashKey(change.item());
                case REMOVED -> {}
            }
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
    public boolean containsKey(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean containsValue(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public V get(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public V put(K k, V v) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public V remove(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Collection<V> values() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    protected void _addHashKey(V item) {
        
    }

    protected void _removeHashKey(V item) {
        
    }
    
}
