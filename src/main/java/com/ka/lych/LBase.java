package com.ka.lych;

import com.ka.lych.util.LJsoperties;
import com.ka.lych.util.LLog;
import com.ka.lych.util.LOS;
import com.ka.lych.util.LPlugins;
import com.ka.lych.util.LReflections;
import com.ka.lych.util.LResources;

/**
 *
 * @author klausahrenberg
 */
public abstract class LBase {

    public static LBase base;
    protected static String appName;
    protected static String version = "0.01";
    protected static String author = "Klaus Ahrenberg";
    protected static String copyRight = "Copyright © 2020 " + author;
    protected static String userName;
    protected static LOS os;
    protected static LPlugins plugins;
    protected static LJsoperties settings = null;
    protected static LResources resources = null;    
    protected boolean stopping;

    public LBase() {
        stopping = false;
        appName = this.createAppName();
        userName = System.getProperties().getProperty("user.name").toLowerCase();
        LLog.APP_TAG = appName;
        resources = new LResources(this.getClass().getPackage().getName());
        this.setSystemSpecificProperties();
        settings = new LJsoperties(appName);
        plugins = new LPlugins();
        plugins.load();
        //LBase.getResources().addResBundle(getClass().getPackage().getName(), getClass().getClassLoader());
    }    
    
    protected String createAppName() {
        try { 
            var classUtils = Class.forName("org.springframework.util.ClassUtils");
            var method = classUtils.getMethod("getUserClass", Class.class);
            Class userClass = (Class) method.invoke(null, getClass());
            return userClass.getSimpleName();
        } catch (Exception ex) {        
            return getClass().getSimpleName();
        }        
    }
    
    /*@PostConstruct
    protected void launchFromSpringBoot() {
        LLog.debug(this, "LBase starts for SpringBoot...", this.getClass().getSimpleName());
        this.start();
    }   
    
    @PreDestroy
    protected void destroyFromSpringBoot() {
        LLog.debug(this, "LBase should end now, says SpringBoot...");
        this.stop();
    }*/

    public abstract void start();

    public abstract void stop();

    /*public static void launch() {
        LBase.getRootUiAdapter().addChildren(new LObservables(LBase.base, ILConstants.ACTION_EXIT_PROGRAM, event -> LBase.exit(0)));
        plugins.start();
        LBase.base.start();
    }*/

    public static void exit(int status) {
        LBase.base.stopping = true;
        plugins.stop();
        LBase.base.stop();
        if (status == 0) {
            //LBase.getSettings().saveSettings();
        }
        System.exit(status);
    }
    
    public static boolean isStopping() {
        return LBase.base.stopping;
    }

    protected static void setSystemSpecificProperties() {
        if (System.getProperty("os.name").toLowerCase().startsWith("mac")) {
            os = LOS.MAC;
        } else if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
            os = LOS.WINDOWS;
        } else if (System.getProperty("os.name").toLowerCase().startsWith("linux")) {
            os = LOS.LINUX;
        } else {
            os = LOS.OTHER;
        }
        if (os == LOS.MAC) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            try {
                /*LMacAdapter.setAboutHandler(this, event -> showAboutInformation.handle(null));
                LMacAdapter.setPreferencesHandler(this, event -> preferencesMenuClick());
                LMacAdapter.setQuitHandler(this, event -> exit(0));
                LMacAdapter.setDockIconImage(getRessources().getImage(this, appName));*/
            } catch (Exception e) {
                LLog.error("Error while loading the OSXAdapter:", e);
            }
        }
    }

    public static String getAuthor() {
        return author;
    }

    public static String getCopyRight() {
        return copyRight;
    }

    public static String getVersion() {
        return version;
    }

    public static LOS getOS() {
        if (os == null) {
            setSystemSpecificProperties();
        }
        return os;
    }

    public static LJsoperties getSettings() {
        return settings;
    }

    public static LResources getResources() {
        return resources;
    }

    public static String getAppName() {
        return appName;
    }

    public static void setAppName(String appName) {
        LBase.appName = appName;
    }

    public static String getAppnameFormatted() {
        return LBase.appName.toLowerCase().replaceAll(" ", "");
    }

    public static String getUserName() {
        return userName;
    }    

    public static LPlugins getPlugins() {
        return plugins;
    }    

    protected static void launch(Class baseClass, String... args) {
        try {
            if (LBase.class.isAssignableFrom(baseClass)) {
                System.setProperty("com.apple.mrj.application.apple.menu.about.name", baseClass.getSimpleName());
                base = (LBase) LReflections.newInstance(baseClass);
            } else {
                throw new RuntimeException("Error: " + baseClass + " is not a subclass of " + LBase.class.getName());
            }            
            base.start();            
        } catch (RuntimeException ex) {
            System.err.println(ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

}
