package com.ka.lych.util;

import com.ka.lych.observable.LString;
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
    
    public static <T> T requireClass(T obj, Class requiredClass) {
        return requireClass(obj, requiredClass, null);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T requireClass(T obj, Class requiredClass, String message) {   
        LObjects.requireNonNull(obj, message);
        if (!requiredClass.isAssignableFrom(obj.getClass())) {
            throw new IllegalArgumentException(LString.isEmpty(message) ? "Required class is " + requiredClass + ". Object is not of this type: " + obj.getClass() : message);
        }
        return obj;
    }
    
}
