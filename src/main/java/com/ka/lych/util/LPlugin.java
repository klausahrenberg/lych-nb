package com.ka.lych.util;

import java.net.URLClassLoader;
import java.util.ResourceBundle;
import com.ka.lych.LBase;
import com.ka.lych.list.LYoso;
import com.ka.lych.observable.LBoolean;
import com.ka.lych.observable.LString;

/**
 *
 * @author klausahrenberg
 */
public abstract class LPlugin extends LYoso {
    
    protected URLClassLoader pluginClassLoader = null;
    protected String pluginBundleName = null;
    protected ResourceBundle pluginBundle;
    protected LBoolean enabled;

    public LPlugin() {
        
    }
    
    protected abstract void start();
    
    public void startPlugin() {
        start();
        setEnabled(true);
    }
    
    protected abstract void stop();
    
    public void stopPlugin() {
        setEnabled(false);
        stop();
    }
    
    public LBoolean observableEnabled() {
        if (enabled == null) {
            enabled = new LBoolean(false);
        }
        return enabled;
    }
    
    public boolean isEnabled() {
        return (enabled != null ? observableEnabled().get() : false);
    }
    
    public void setEnabled(boolean enabled) {
        observableEnabled().set(enabled);        
    }    

    public URLClassLoader getPluginClassLoader() {
        return pluginClassLoader;
    }

    public String getPluginBundleName() {
        return pluginBundleName;
    }

    public ResourceBundle getPluginBundle() {
        return pluginBundle;
    }

    public void loadLanguageBundle() {
        pluginBundle = null;
        if ((pluginClassLoader != null) && (!LString.isEmpty(pluginBundleName))) {    
            try {
                pluginBundle = ResourceBundle.getBundle(pluginBundleName + ".resources.language", LBase.getResources().getLocale(), pluginClassLoader);     
            } catch (Exception e) { }
        }
    }
    
}
