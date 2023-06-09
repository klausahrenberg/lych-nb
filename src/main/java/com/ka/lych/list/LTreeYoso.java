package com.ka.lych.list;

import com.ka.lych.observable.LBoolean;
import com.ka.lych.observable.LObservable;

/**
 *
 * @author klausahrenberg
 * @param <K>
 * @param <V>
 */
public class LTreeYoso<K, V extends LTreeYoso<K, V>> extends LHashYoso<K>
        implements ILTreeYoso<K, V> {

    private final boolean DEFAULT_HAS_CHILDRENS = false;
    private final ILHashYosos<K, V> DEFAULT_CHILDRENS = null;
    protected LObservable<ILHashYosos<K, V>> childrens;
    protected LBoolean hasChildrens;
    protected ILYososChangeListener<V> yososListener = change -> updateHasChildrens();

    public LTreeYoso() {
        this(null);
    }

    public LTreeYoso(K hashKey) {
        super(hashKey);
    }

    @Override
    @SuppressWarnings("unchecked")
    public LObservable<ILYosos<V>> childrens() {
        if (childrens == null) {
            childrens = new LObservable<>(DEFAULT_CHILDRENS);
            childrens.addListener(change -> {
                if (change.getOldValue() != null) {
                    change.getOldValue().removeListener(yososListener);
                }
                if (change.getNewValue() != null) {
                    change.getNewValue().addListener(yososListener);
                }
                updateHasChildrens();
            });
        }
        return (LObservable) childrens;
    }

    @Override
    public boolean loadChildrens() {
        return true;
    }

    @Override
    public ILHashYosos<K, V> getChildrens() {
        return (childrens != null ? childrens.get() : DEFAULT_CHILDRENS);
    }

    public void setChildrens(ILHashYosos<K, V> childrens) {
        childrens().set(childrens);
        updateHasChildrens();
    }

    public void addChildren(V children) {
        if (getChildrens() == null) {
            setChildrens(new LHashYosos<>());
        }
        getChildrens().add(children);
    }

    public LBoolean observableHasChildrens() {
        if (hasChildrens == null) {
            hasChildrens = new LBoolean(DEFAULT_HAS_CHILDRENS);
        }
        return hasChildrens;
    }

    @Override
    public boolean hasChildrens() {
        return hasChildrens != null && hasChildrens.isPresent() ? hasChildrens.get() : DEFAULT_HAS_CHILDRENS;
    }

    protected void updateHasChildrens() {
        boolean hc = getChildrens() != null && getChildrens().size() > 0;
        if (hc != DEFAULT_HAS_CHILDRENS) {
            observableHasChildrens().set(hc);
        }
    }

}
