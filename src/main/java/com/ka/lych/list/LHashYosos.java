package com.ka.lych.list;

import java.io.*;
import java.util.*;
import com.ka.lych.event.LObservableChangeEvent;
import com.ka.lych.exception.LDoubleHashKeyException;
import com.ka.lych.exception.LValueException;
import com.ka.lych.observable.ILChangeListener;
import com.ka.lych.observable.ILObservable;
import com.ka.lych.observable.ILValidator;

/**
 *
 * @author klausahrenberg
 * @param <V> yoso
 * @param <K> key
 */
public class LHashYosos<K, V extends ILHashYoso> extends LYosos<V>
        implements ILHashYosos<K, V>, Serializable {

    protected ArrayList<LxHashIndex<K, V>> hashIndexes = new ArrayList<>();

    private final ILValidator<K, ILObservable> hashYosoAcceptor = (LObservableChangeEvent<K, ILObservable> change) -> {
        return (existsHashKey(change.newValue())
                ? new LValueException("Key already exists: %s", change.newValue())
                : null);
    };

    @SuppressWarnings("unchecked")
    private final ILChangeListener<K, ILObservable> hashYosoListener = change -> {
        if (change.oldValue() != null) {
            this.removeHashKey(change.oldValue());
        }
        if (change.newValue() != null) {
            try {
                this.addHashKey((V) change.source());
            } catch (LDoubleHashKeyException dhke) {
                //impossible state
                throw new IllegalStateException(dhke.getMessage(), dhke);
            }
        }
    };

    public LHashYosos() {
        this(128);
    }

    public LHashYosos(int hashIndexSize) {
        this.addHashIndex(hashIndexSize);
    }

    public void addHashIndex(int hashIndexSize) {
        LxHashIndex<K, V> hi = new LxHashIndex<>(hashIndexSize);
        hashIndexes.add(hi);
    }

    @Override
    public V get(int index) {
        return super.get(index);
    }

    @Override
    public V get(K key) {
        return get(key, false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(K key, boolean recursive) {
        V hashYoso = hashIndexes.get(0).getHashYoso(key);
        if ((recursive) && (hashYoso == null)) {
            Iterator<V> it_y = this.iterator();
            while ((hashYoso == null) && (it_y.hasNext())) {
                V y = it_y.next();
                if (y instanceof ILTreeYoso) {
                    if ((((ILTreeYoso) y).hasChildrens()) && (((ILTreeYoso) y).getChildrens() instanceof ILHashYosos)) {
                        hashYoso = ((ILHashYosos<K, V>) ((ILTreeYoso) y).getChildrens()).get(key);
                    }
                } else {
                    break;
                }
            }
        }
        return hashYoso;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void add(int index, V hashYoso) {
        try {
            this.addHashKey(hashYoso);
            super.add(index, hashYoso);
            hashYoso.hashKey().addAcceptor(hashYosoAcceptor);
            hashYoso.hashKey().addListener(hashYosoListener);
        } catch (LDoubleHashKeyException ex) {
            if (this.contains(hashYoso)) {
                throw new IllegalArgumentException("HashYoso is already in this list. hashKey: '" + hashYoso.getHashKey() + "' / hashYoso: " + hashYoso, ex);
            } else {
                throw new IllegalArgumentException("HashYoso has already an equal key in list. hashKey: '" + hashYoso.getHashKey() + "' / hashYoso: " + hashYoso, ex);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public V remove(int index) {
        V hashYoso = this.get(index);
        this.removeHashKey((K) hashYoso.getHashKey());
        hashYoso.hashKey().removeListener(hashYosoListener);
        hashYoso.hashKey().removeAcceptor(hashYosoAcceptor);
        return super.remove(index);
    }

    public void changeKey(K oldKey, K newKey) throws LDoubleHashKeyException {
        hashIndexes.get(0).changeHashKey(oldKey, newKey);
    }

    @Override
    public void clear() {
        for (int i = 0; i < hashIndexes.size(); i++) {
            hashIndexes.get(i).clear();
        }
        super.clear();
    }

    @Override
    public boolean removeByKey(K key) {
        V hashYoso = this.get(key);
        if (hashYoso != null) {
            return remove(hashYoso);
        } else {
            return false;
        }
    }

    public boolean removeYosoWithKey(K key) {
        return this.removeByKey(key);
    }

    protected void addHashKey(V hashYoso) throws LDoubleHashKeyException {
        if (hashYoso.getHashKey() != null) {
            hashIndexes.get(0).addHashKey(hashYoso);
        }
    }

    protected void removeHashKey(K key) {
        if (key != null) {
            hashIndexes.get(0).removeHashKey(key);
        }
    }

    protected boolean existsHashKey(K key) {
        return (key != null ? hashIndexes.get(0).existsHashKey(key) : false);
    }

    protected class LxHashIndex<K, V extends ILHashYoso> {

        private final LxHashIndexItem<K, V>[] hashItems;
        private final int initialCapacity;

        @SuppressWarnings("unchecked")
        public LxHashIndex(int initialCapacity) {
            this.initialCapacity = initialCapacity;
            this.hashItems = new LxHashIndexItem[initialCapacity];
        }

        private int getHash(K key) {
            return Math.abs(key.hashCode()) % initialCapacity;
        }

        private LxHashIndexItem<K, V> getHashItem(K key) {
            return getHashItem(key, getHash(key));
        }
        
        @SuppressWarnings("unchecked")
        private LxHashIndexItem<K, V> getHashItem(K key, int hash) {
            LxHashIndexItem<K, V> result = null;
            LxHashIndexItem<K, V> hi = hashItems[hash];
            while (hi != null) {
                if ((hi.yoso != null) && (hi.yoso.getHashKey().equals(key))) {
                    result = hi;
                    break;
                } else {
                    hi = hi.next;
                }
            }
            return result;
        }

        public V getHashYoso(K key) {
            LxHashIndexItem<K, V> hi = this.getHashItem(key);
            if (hi != null) {
                return hi.yoso;
            } else {
                return null;
            }
        }

        public boolean existsHashKey(K key) {
            return existsHashKey(key, getHash(key));
        }

        public boolean existsHashKey(K key, int hash) {
            return (this.getHashItem(key, hash) != null);
        }

        public void addHashKey(V value) throws LDoubleHashKeyException {
            @SuppressWarnings("unchecked")
            K key = (K) value.getHashKey();
            int hash = getHash(key);
            if (this.existsHashKey(key, hash)) {
                throw new LDoubleHashKeyException("Key '%s' already exists. (hash: %s)", key, hash);
            }
            LxHashIndexItem<K, V> hi = new LxHashIndexItem<>(value);
            hi.next = hashItems[hash];
            hashItems[hash] = hi;
        }

        @SuppressWarnings("unchecked")
        public void removeHashKey(K key) {
            int hash = getHash(key);
            LxHashIndexItem last = null;
            LxHashIndexItem hi = hashItems[hash];
            while (hi != null) {
                if (!hi.yoso.getHashKey().equals(key)) {
                    last = hi;
                    hi = hi.next;
                } else {
                    if (last == null) {
                        hashItems[hash] = hi.next;
                    } else {
                        last.next = hi.next;
                    }
                    hi.yoso = null;
                    hi.next = null;
                    hi = null;
                }
            }
        }

        public void changeHashKey(K oldKey, K newKey)
                throws LDoubleHashKeyException {
            int newHash = getHash(newKey);
            if (this.existsHashKey(newKey, newHash)) {
                throw new LDoubleHashKeyException("Can't modify existing key '%s' to new key '%s'", oldKey, newKey);
            }
            V value = this.getHashYoso(oldKey);
            // removeOld
            this.removeHashKey(oldKey);
            // newKey
            LxHashIndexItem<K, V> hi = new LxHashIndexItem<>(value);
            hi.next = hashItems[newHash];
            hashItems[newHash] = hi;
        }

        @SuppressWarnings("unchecked")
        public void clear() {
            for (int i = 0; i < initialCapacity; i++) {
                LxHashIndexItem hi = hashItems[i];
                while (hi != null) {
                    LxHashIndexItem t = hi.next;
                    hi.yoso = null;
                    hi.next = null;
                    hi = null;
                    hi = t;
                }
                hashItems[i] = null;
            }
        }
    }

    protected class LxHashIndexItem<K, V extends ILHashYoso> {

        public LxHashIndexItem next;
        public V yoso;

        public LxHashIndexItem(V yoso) {
            this.yoso = yoso;
        }

    }
}
