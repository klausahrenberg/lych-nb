package com.ka.lych.util;

import java.util.*;
import com.ka.lych.LBase;
import com.ka.lych.observable.LString;
import com.ka.lych.list.LList;
import com.ka.lych.list.LMap;
import com.ka.lych.observable.LObject;

/**
 *
 * @author klausahrenberg
 */
public class LResources
        implements ILResources {

    protected final List<Locale> supportedLocales;
    private LObject<Locale> locale;
    private ResourceBundle bundle;

    /**
     * Creates a new instance of LResources
     *
     * @param bundleName
     */
    public LResources(String bundleName) {
        super();
        supportedLocales = LList.empty();
        supportedLocales.add(Locale.US);
    }

    private String buildGroupKey(Object sender) {
        String groupKey;
        if (sender == null) {
            groupKey = null;
        } else if (sender instanceof Class) {
            groupKey = ((Class<?>) sender).getName();
        } else {
            groupKey = sender.getClass().getName();
        }
        return groupKey;
    }

    protected Package getPackage(Object sender) {
        if (sender == null) {
            sender = LBase.class;
        }
        return (sender instanceof Class ? ((Class) sender).getPackage() : sender.getClass().getPackage());
    }

    public ResourceBundle getBundle(Class sender) {
        if (bundle == null) {
            try {
                //bundle = ResourceBundle.getBundle("language" , getLocale());
                bundle = ResourceBundle.getBundle("languages.language" , getLocale());
            } catch (MissingResourceException mre) {
                mre.printStackTrace();
                bundle = ResourceBundle.getBundle(sender.getPackageName().concat(".res.language") , getLocale());
            }    
        }
        return bundle;
    }    

    @Override
    public LObject<Locale> observableLocale() {
        if (locale == null) {
            locale = new LObject<>(Locale.getDefault());
            locale.addListener(change -> bundle = null);
        }
        return locale;
    }

    @Override
    public Locale getLocale() {
        return (locale != null ? locale.get() : Locale.getDefault());
    }

    @Override
    public void setLocale(Locale locale) {
        observableLocale().set(locale);
    }

    @Override
    public Object get(Object sender, String ressourceKey, Class<?> ressourceClass) throws LValueNotFoundException {
        if (ressourceClass == null) {
            throw new IllegalArgumentException("ressourceClass for key '" + ressourceKey + "' is null.");
        }
        if (String.class.isAssignableFrom(ressourceClass)) {
            return localize(sender, ressourceKey, null);
        } else {
            throw new UnsupportedOperationException("class '" + ressourceClass.getName() + "' not supported yet");
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public String localize(Object source, String key, Object values) {
        String extKey = this.buildGroupKey(source);
        extKey = (extKey != null ? extKey + "." : "") + key;
        ResourceBundle bundle = this.getBundle(source != null ? ((source instanceof Class ? (Class) source : source.getClass())) : null);
        boolean isTooltip = extKey.endsWith(ILConstants.DOT + ILConstants.TOOLTIP);
        if (bundle != null) {
            try {
                String s = bundle.getString(extKey);
                if (s != null) {                    
                    try {
                        if (values instanceof LMap) {
                            return LString.format(s, (LMap) values);
                        } else if (values instanceof LList) {
                            return LString.format(s, (LList) values);
                        } else {
                            return LString.format(s, values);
                        }
                    } catch (IllegalFormatException ife) {
                        LLog.error(this, "String format exception for '" + extKey + "': " + ife.getMessage());
                    }    
                }                    
            } catch (MissingResourceException mre) {
                if (!isTooltip) {
                    LLog.debug(this, "Language item for key '" + extKey + "' not found.");
                }
            }
        } 
        return (!isTooltip ? key : null);        
    }        

    /*public String format(Object sender, String ressourceKey, Object... placeHolderValues) {
        String result = localize(sender, ressourceKey, null, true);
        if ((result != null) && (placeHolderValues != null)) {
            result = MessageFormat.format(result, placeHolderValues);
        }
        return result;
    }*/

    @Override
    public List<Locale> getSupportedLocales() {
        return supportedLocales;
    }

    @Override
    public void addSupportedLocale(Locale loc) {
        Locale current = getLocale();
        this.supportedLocales.add(current.equals(loc) ? current : loc);
    }

    @Override
    public void removeSupportedLocale(Locale loc) {
        this.supportedLocales.remove(loc);
    }

}
