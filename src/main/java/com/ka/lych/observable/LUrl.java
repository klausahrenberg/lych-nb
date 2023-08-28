package com.ka.lych.observable;

import java.net.MalformedURLException;
import java.net.URL;
import com.ka.lych.util.ILConstants;

/**
 *
 * @author klausahrenberg
 */
public class LUrl extends LString {

    private URL url;
    
    private final ILChangeListener<String, LString> valueListener = change -> {
        url = null;
    };

    public LUrl() {
        initialize();
    }

    public LUrl(String initialValue) {
        super(initialValue);
        initialize();
    }

    final private void initialize() {
        this.addListener(valueListener);
    }

    @Override
    protected String _preconfigureValue(String newValue) {        
        if ((newValue != null) && (newValue.trim().startsWith(ILConstants.KEYWORD_RESOURCE_URL))) {
            newValue = newValue.substring(ILConstants.KEYWORD_RESOURCE_URL.length());
            if (newValue.startsWith(ILConstants.SLASH)) {
                newValue = newValue.substring(ILConstants.SLASH.length());
            }            
            URL u = ClassLoader.getSystemResource(newValue);
            return (u != null ? u.toString() : newValue);
        } else {
            return super._preconfigureValue(newValue);
        }
    }    

    public URL getURL() {
        if ((url == null) && (!LString.isEmpty(get()))) {
            try {                
                url = new URL(get());
            } catch (MalformedURLException mfue) {
                //maybe a resource
                url = getClass().getClassLoader().getResource(get());
                if (url == null) {
                    throw new IllegalArgumentException("Can't load url: " + get(), mfue);
                }
            }
        }
        return url;
    }

}
