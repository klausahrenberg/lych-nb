package com.ka.lych.util;

import java.util.function.Consumer;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
@FunctionalInterface
public interface ILConsumer<T> {

    void acceptOrElseThrow(T t) throws Exception;
    
    public default void accept(T t) {
        try {
            acceptOrElseThrow(t);
        } catch (Exception ex) {
            throw new LUnchecked(this, ex.getMessage(), ex);
        }
    }

    public static <T> Consumer<T> accept(ILConsumer<T> throwingConsumer) {
        return i -> throwingConsumer.accept(i);
    }
    
}
