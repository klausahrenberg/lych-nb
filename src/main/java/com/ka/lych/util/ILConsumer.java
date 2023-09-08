package com.ka.lych.util;

import java.util.function.Consumer;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
@FunctionalInterface
public interface ILConsumer<T, E extends Exception> {

    void acceptOrElseThrow(T t) throws E;
    
    public default void accept(T t) {
        try {
            acceptOrElseThrow(t);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static <T> Consumer<T> accept(ILConsumer<T, Exception> throwingConsumer) {
        return i -> throwingConsumer.accept(i);
    }
    
}
