package com.ka.lych.ui;

import java.util.Iterator;
import com.ka.lych.list.LMap;
import com.ka.lych.list.LYoso;
import com.ka.lych.observable.LObservable;
import com.ka.lych.observable.ILObservables;

/**
 *
 * @author klausahrenberg
 * @param <T> Value type of observable
 */
public interface ILSupportsObservables<T> extends ILControl {
    
    /**
     * Gives access to the observable of value which is related to the control. 
     * Never instanciate at call of this method - if the observable is null, return null.
     * @return the observable, can be null
     */
    public ILObservables getObservables();

    public void setObservables(ILObservables observables);

    public default T getValue() {
        @SuppressWarnings("unchecked")
        LObservable<T> obs = (getObservables() != null ? getObservables().observable(id().orElseThrow()) : null);        
        return obs != null ? obs.get() : null;
    }

    /**
     * Called by MListCell for example
     * @param pane
     * @param yoso 
     */
    public static void linkPaneToObservables(ILChildrensIterable pane, LYoso yoso) {
        if (yoso == null) {
            throw new IllegalArgumentException("Yoso can't be null.");
        }
        Iterator it_c = pane.getChildrensIterator();
        while (it_c.hasNext()) {
            Object c = it_c.next();
            if (c != null) {
                //assign observables                
                if ((c instanceof ILSupportsObservables) && (((ILSupportsObservables) c).getObservables() == null) && (((ILSupportsObservables) c).id().isPresent())) {
                    ((ILSupportsObservables) c).setObservables(yoso);                    
                }
                if (ILChildrensIterable.class.isAssignableFrom(c.getClass())) {
                    linkPaneToObservables((ILChildrensIterable) c, yoso);
                }
            }
        }
    }

    public static void unlinkPaneFromObservables(ILChildrensIterable pane, LYoso oldYoso) {
        if (pane != null) {
            Iterator it_c = pane.getChildrensIterator();
            while (it_c.hasNext()) {
                Object c = it_c.next();
                if (c != null) {
                    //assign observables
                    if ((c instanceof ILSupportsObservables) && ((oldYoso == null) || (((ILSupportsObservables) c).getObservables() == oldYoso))) {
                        ((ILSupportsObservables) c).setObservables(null);
                    }
                    if (ILChildrensIterable.class.isAssignableFrom(c.getClass())) {
                        unlinkPaneFromObservables((ILChildrensIterable) c, oldYoso);
                    }
                }
            }
        }
    }
    
    public static void linkControlToObservable(ILSupportsObservables control, ILObservables observables) {        
        control.setObservables(observables);        
    }
    
    public static void linkPaneToObservables(ILChildrensIterable pane, LMap<String, ILObservables> observables) {
        Iterator it_c = pane.getChildrensIterator();
        while (it_c.hasNext()) {
            Object c = it_c.next();
            if (c != null) {
                //assign observables                
                if ((c instanceof ILSupportsObservables) && ((ILSupportsObservables) c).id().isPresent()) {
                    ILObservables observable = (observables != null ? observables.get(((ILSupportsObservables) c).id().get()) : null);                        
                    if (observable != null) {
                        ((ILSupportsObservables) c).setObservables(observable);
                    //} else if ((observables != null) && (observables.size() > 0)) {
                    //    LLog.error(ILSupportsObservables.class, "No value found for id: '" + id + "' / control: " + c);                        
                    }                                     
                }
                if (ILChildrensIterable.class.isAssignableFrom(c.getClass())) {
                    linkPaneToObservables((ILChildrensIterable) c, observables);
                }
            }
        }
    }

}
