package com.ka.lych.util;

import java.util.Objects;

/**
 *
 * @author klausahrenberg
 */
public abstract class LObjects {
    
    public static <T> T requireNonNull(T obj) {
        return Objects.requireNonNull(obj);
    }

    public static <T> T requireNonNull(T obj, String message) {        
        return Objects.requireNonNull(obj, message);
    }
    
    public static <T> T requireInstanceOf(Class objClass, T obj) {
        if (!objClass.isInstance(obj)) {
            throw new IllegalArgumentException(obj + " is not right class type: " + obj.getClass() + ". Required type is: " + objClass);
        }
        return obj;
    }
    
}
