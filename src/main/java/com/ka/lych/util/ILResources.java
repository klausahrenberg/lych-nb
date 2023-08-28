package com.ka.lych.util;

import com.ka.lych.observable.LObject;
import java.util.Locale;
import java.util.List;

/**
 *
 * @author klausahrenberg
 */
public interface ILResources {

    public LObject<Locale> observableLocale();
    
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
