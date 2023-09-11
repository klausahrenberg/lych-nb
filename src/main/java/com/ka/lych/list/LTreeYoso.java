package com.ka.lych.list;

import com.ka.lych.observable.LBoolean;
import com.ka.lych.observable.LObject;
import com.ka.lych.util.ILConsumer;

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
    protected LObject<ILHashYosos<K, V>> childrens;
    protected LBoolean hasChildrens;
    protected ILConsumer<LListChange<V>> yososListener = change -> updateHasChildrens();            

    public LTreeYoso() {
        this(null);
    }

    public LTreeYoso(K hashKey) {
        super(hashKey);
    }

    @Override
    @SuppressWarnings("unchecked")
    public LObject<ILYosos<V>> childrens() {
        if (childrens == null) {
            childrens = new LObject<>(DEFAULT_CHILDRENS);
            childrens.addListener(change -> {
                if (change.oldValue() != null) {
                    change.oldValue().removeListener(yososListener);
                }
                if (change.newValue() != null) {
                    change.newValue().addListener(yososListener);
                }
                updateHasChildrens();
            });
        }
        return (LObject) childrens;
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
