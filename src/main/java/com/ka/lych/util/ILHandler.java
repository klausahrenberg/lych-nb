package com.ka.lych.util;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
@FunctionalInterface
public interface ILHandler<T> {
    
    public void handle(T t);
    
}
