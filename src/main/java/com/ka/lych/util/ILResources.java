package com.ka.lych.util;

import java.util.Locale;
import com.ka.lych.observable.LObservable;
import java.util.List;

/**
 *
 * @author klausahrenberg
 */
public interface ILResources {

    public LObservable<Locale> observableLocale();
    
    public Locale getLocale();
    
    public void setLocale(Locale locale);
    
    public List<Locale> getSupportedLocales();

    public void addSupportedLocale(Locale loc);

    public void removeSupportedLocale(Locale loc);

    public Object get(Object sender, String ressourceKey, Class<?> ressourceClass) throws LValueNotFoundException;

    public default String localize(Object source, String key) {
        return localize(source, key, null);
    }
    
    public String localize(Object source, String key, Object values);

}
