package com.ka.lych.list;

import com.ka.lych.observable.LObject;
import com.ka.lych.util.LFuture;
import com.ka.lych.util.LLog;
import java.util.function.Function;

/**
 * Creates a picked list (e.g.a favorite list) out from a source list picked by
 * a selector, which can be another data type.A converter and reverse converts
 * the keys from selector to source key and back.
 *
 * @author klausahrenberg
 * @param <T> selector class type
 * @param <V> value class type
 */
public class LPick<T extends LYoso, V extends LTYoso<V>> extends LKeyYosos<V> {

    private final Function<T, Object[]> DEFAULT_CONVERTER = null;
    private final Function<Object[], T> DEFAULT_ADDER = null;
    private final Function<Object[], T> DEFAULT_REMOVER = null;
    private final LTYoso<V> DEFAULT_SOURCE = null;
    private final LKeyYosos<T> DEFAULT_SELECTOR = null;
    protected LObject<LTYoso<V>> source;
    protected LObject<LKeyYosos<T>> selector;
    protected LObject<Function<T, Object[]>> converter;
    protected LObject<Function<Object[], T>> adder;
    protected LObject<Function<Object[], T>> remover;
    private boolean updating;

    public LPick() {
        updating = false;
        this.addListener(change -> {
            switch (change.type()) {
                case ADDED ->
                    this.addToPick(change.item());
                case REMOVED ->
                    this.removeFromPick(change.item());
                //case CHANGED                 
                }
        });
    }

    @SuppressWarnings("unchecked")
    public LObject<LTYoso<V>> source() {
        if (source == null) {
            source = new LObject(DEFAULT_SOURCE);
            source.addListener(change -> update());
        }
        return source;
    }

    public LTYoso<V> getSource() {
        return source != null ? source.get() : DEFAULT_SOURCE;
    }

    public void setSource(LTYoso<V> source) {
        source().set(source);
    }

    @SuppressWarnings("unchecked")
    public LObject<LKeyYosos<T>> selector() {
        if (selector == null) {
            selector = new LObject(DEFAULT_SELECTOR);
            selector.addListener(change -> update());
        }
        return selector;
    }

    public LKeyYosos<T> getSelector() {
        return selector != null ? selector.get() : DEFAULT_SELECTOR;
    }

    public void setSelector(LKeyYosos<T> selector) {
        selector().set(selector);
    }

    @SuppressWarnings("unchecked")
    public LObject<Function<T, Object[]>> converter() {
        if (converter == null) {
            converter = new LObject(DEFAULT_CONVERTER);
            converter.addListener(change -> update());
        }
        return converter;
    }

    public Function<T, Object[]> getConverter() {
        return converter != null ? converter.get() : DEFAULT_CONVERTER;
    }

    public void setConverter(Function<T, Object[]> converter) {
        converter().set(converter);
    }

    @SuppressWarnings("unchecked")
    public LObject<Function<Object[], T>> adder() {
        if (adder == null) {
            adder = new LObject(DEFAULT_ADDER);
            adder.addListener(change -> update());
        }
        return adder;
    }

    public Function<Object[], T> getAdder() {
        return adder != null ? adder.get() : DEFAULT_ADDER;
    }

    public void setAdder(Function<Object[], T> adder) {
        adder().set(adder);
    }

    @SuppressWarnings("unchecked")
    public LObject<Function<Object[], T>> remover() {
        if (remover == null) {
            remover = new LObject(DEFAULT_REMOVER);
            //remover.addListener(change -> update());
        }
        return remover;
    }

    public Function<Object[], T> getRemover() {
        return remover != null ? remover.get() : DEFAULT_REMOVER;
    }

    public void setRemover(Function<Object[], T> remover) {
        remover().set(remover);
    }

    protected void update() {
        if ((getSource() != null) && (getSelector() != null) && (getConverter() != null) && getAdder() != null) {
            this.updating = true;
            this.clear();
            getSelector().forEach(selecto -> {
                if ((getSource().hasChildrens()) && (getSource().getChildrens() instanceof LKeyYosos)) {
                    LKeyYosos<V> sourceList = (LKeyYosos<V>) getSource().getChildrens();
                    Object[] keys = getConverter().apply(selecto);
                    if (keys != null) {
                        V yoso = sourceList.get(keys);
                        if (yoso != null) {
                            this.add(yoso);
                        }
                    }
                }
            });
            this.updating = false;
        }
    }

    private void addToPick(V yoso) {
        if (!updating) {
            if ((yoso != null) && (getSelector() != null) && (getAdder() != null)) {
                //Call Reverse, should add, if not exists
                //getAdder().convert(yoso.getHashKey());                            
                throw new UnsupportedOperationException("tbi again");
            } else {
                LLog.error("Yoso, Selector and/or Adder missing.");
            }
        }
    }

    private void removeFromPick(V yoso) {
        if (!updating) {
            if ((yoso != null) && (getSelector() != null) && (getRemover() != null)) {
                //getRemover().convert(yoso.getHashKey());         
                throw new UnsupportedOperationException("tbi again");
            } else {
                LLog.error("Yoso, Selector and/or Remover missing.");
            }
        }
    }

}
