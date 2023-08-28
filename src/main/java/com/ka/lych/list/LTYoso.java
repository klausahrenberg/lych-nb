package com.ka.lych.list;

import com.ka.lych.observable.LInteger;
import com.ka.lych.observable.LObject;
import com.ka.lych.util.ILSupportsChildrens;
import com.ka.lych.util.LReflections;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public class LTYoso<T extends LTYoso> extends LYoso
        implements ILSupportsChildrens<T, LKeyYosos<T>> {

    private final boolean DEFAULT_HAS_CHILDRENS = false;
    private final LKeyYosos<T> DEFAULT_CHILDRENS = null;
    private final Integer DEFAULT_CHILD_COUNT = null;
    protected LObject<LKeyYosos<T>> childrens;    
    protected LInteger childCount;    

    public LTYoso() {

    }

    @Override
    public LObject<LKeyYosos<T>> childrens() {
        if (childrens == null) {
            childrens = new LObject<>(DEFAULT_CHILDRENS);
            childrens.addListener(c -> {
                c.ifOldValueExists(o -> o.setParent(null));
                c.ifNewValueExists(n -> n.setParent(this));
            });
        }
        return childrens;
    }

    @Override
    public boolean loadChildrens() {
        return true;
    }

    @Override
    public LKeyYosos<T> getChildrens() {
        return (childrens != null ? childrens.get() : DEFAULT_CHILDRENS);
    }

    public void setChildrens(LKeyYosos<T> childrens) {
        childrens().set(childrens);        
    }

    public void ensureChildrens() {
        if (getChildrens() == null) {
            Class treeDataClass = LReflections.getParameterClass(getClass(), 0);
            @SuppressWarnings("unchecked")
            LKeyYosos<T> childs = (LKeyYosos<T>) LReflections.newInstance(LKeyYosos.class, treeDataClass);
            setChildrens(childs);
            //setChildrens(new LKeyYosos<>());
        }
    }

    public void addChildren(T children) {
        ensureChildrens();
        getChildrens().add(children);
    }

    @Override
    public boolean hasChildrens() {
        return (getChildCount() > 0);
    }   

    public LInteger observableChildCount() {
        if (childCount == null) {
            childCount = new LInteger(DEFAULT_CHILD_COUNT);
        }
        return childCount;
    }

    public int getChildCount() {
        return childCount != null ? childCount.get() : (getChildrens() != null ? getChildrens().size() : 0);
    }

    public void setChildCount(Integer childCount) {
        observableChildCount().set(childCount);
    }

}
