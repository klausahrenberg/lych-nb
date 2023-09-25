package com.ka.lych.util;

import java.util.Iterator;
import java.util.ServiceLoader;
import com.ka.lych.list.LYosos;

/**
 *
 * @author Klaus Ahrenberg
 */
public class LPlugins extends LYosos<LPlugin> {

    public LPlugins() {
        super();
    }

    /**
     * Loads external plugins
     */
    public void load() {
        ServiceLoader<LPlugin> sl = ServiceLoader.load(LPlugin.class);        
        Iterator<LPlugin> iter = sl.iterator();
        LPlugin plugin;
        if (!iter.hasNext()) {
            LLog.debug("No plugins found.");
        }
        while (iter.hasNext()) {
            plugin = iter.next();
            plugin.pluginBundleName = plugin.getClass().getPackageName();                        
            plugin.loadLanguageBundle();                        
            //LBase.getResources().addResBundle(plugin.pluginBundleName, plugin.getClass().getClassLoader());
            this.add(plugin); 
            LLog.debug("Plugin '" + plugin.getClass() + "' detected");
        }        
    }

    public void start() {
        for (LPlugin plugin : this) {
            plugin.startPlugin();
        }
    }

    public void stop() {
        for (LPlugin plugin : this) {
            if (plugin.isEnabled()) {
                plugin.stopPlugin();
            }
        }
    }    

    public LPlugin get(Class<?> pluginClass) {
        LPlugin result = null;
        Iterator<LPlugin> itp = iterator();
        while (itp.hasNext()) {
            LPlugin p = itp.next();
            if (p.getClass().equals(pluginClass)) {
                result = p;
                break;
            }
        }
        return result;
    }

}
