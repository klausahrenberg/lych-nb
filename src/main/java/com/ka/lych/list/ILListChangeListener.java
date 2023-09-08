package com.ka.lych.list;

import com.ka.lych.util.LException;
import com.ka.lych.util.LFuture;
import java.util.Collection;

/**
 *
 * @author Klaus Ahrenberg
 * @param <T>
 */
public interface ILListChangeListener<T> {

    public abstract LFuture<Void, ? extends LException> onChanged(LListChange<T> change);

    public enum LChangeType {
        ADDED, REMOVED, CHANGED
    };
    
    public static record LListChange<T>(LChangeType type, Collection<T> list, T item, T oldItem, int index) {
        public boolean isAdded() {
            return (type == LChangeType.ADDED);
        }

        public boolean isRemoved() {
            return (type == LChangeType.REMOVED);
        }

        public boolean isChanged() {
            return (type == LChangeType.CHANGED);
        }
        
    }

}
