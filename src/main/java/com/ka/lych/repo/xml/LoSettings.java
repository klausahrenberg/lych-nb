package com.ka.lych.repo.xml;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.ka.lych.LBase;
import com.ka.lych.geometry.ILBounds;
import com.ka.lych.list.LKeyYosos;
import com.ka.lych.repo.LKeyValue;
import com.ka.lych.util.ILConstants;
import com.ka.lych.util.LLog;

/**
 *
 * @author klausahrenberg
 */
public class LoSettings extends LXmlRepository {

    private static final String DEFAULT_FILE_NAME = "preferences";
    
    protected final String userHomeDirectory, appDirectory;
    protected LKeyYosos<LKeyValue> userValues, globalValues;
    protected File userFile;    

    public LoSettings(String appName) {
        //Global settings inside of program dir, only readable
        appDirectory = System.getProperties().getProperty("user.dir");  
        globalValues = new LKeyYosos<>(LKeyValue.class);//, globalSettingsPath);
        //globalValues.setReadOnly(true);
        this.load(globalValues, new File(appDirectory + File.separator + DEFAULT_FILE_NAME + ILConstants.DOT + ILConstants.KEYWORD_FILE_XML_SUFFIX));
        
        //User-specific settings
        String uh = System.getProperties().getProperty("user.home");        
        switch (LBase.getOS()) {
            case MAC     -> userHomeDirectory = uh + File.separator + "Library" + File.separator + "Application Support" + File.separator + appName;
            case WINDOWS -> userHomeDirectory = uh + File.separator + "AppData" + File.separator + "Roaming" + File.separator + appName;
            default      -> userHomeDirectory = uh + File.separator + "." + appName.toLowerCase();
        }
        this.checkIfDirectoryExists(userHomeDirectory);
        userValues = new LKeyYosos<>(LKeyValue.class);
        userFile = new File(userHomeDirectory + File.separator + DEFAULT_FILE_NAME + ILConstants.DOT + ILConstants.KEYWORD_FILE_XML_SUFFIX);
        this.load(userValues, userFile);
    }
    
    protected void checkIfDirectoryExists(String dirName) {
        File f = new File(dirName);
        if (!f.exists()) {
            try {
                if (!f.mkdir()) {
                    throw new Exception("mkdir failed (" + dirName + ")");
                }
            } catch (Exception e) {
                LLog.error("Can't create folder '" + dirName + "'", e, true);
            }
        }
    }

    public boolean saveSettings() {        
        save(userValues, userFile);        
        return true;
    }

    public String createKey(Object sender, String key) {
        String extkey;
        if (sender == null) {
            extkey = key;
        } else if (sender instanceof Class) {
            extkey = ((Class) sender).getName() + "." + key;
        } else {
            extkey = sender.getClass().getName() + "." + key;
        }
        return extkey;
    }

    public void remove(Object sender, String key) {
        userValues.remove(userValues.get(this.createKey(sender, key)));
        globalValues.remove(globalValues.get(this.createKey(sender, key)));
    }

    public void setBoolean(Object sender, String key, Boolean value) {
        LKeyValue kv = userValues.getOrAdd(this.createKey(sender, key));
        kv.setBooleanValue(value);
    }

    public void setInteger(Object sender, String key, Integer value) {
        LKeyValue kv = userValues.getOrAdd(this.createKey(sender, key));
        kv.setIntegerValue(value);
    }

    public void setString(Object sender, String key, String value) {
        LKeyValue kv = userValues.getOrAdd(this.createKey(sender, key));
        kv.setStringValue(value);
    }

    public LocalDateTime getDatetime(Object sender, String key, LocalDateTime defaultValue) {
        LKeyValue el = get(sender, key);
        return (el != null ? (el.getDatetimeValue() != null ? el.getDatetimeValue() : defaultValue) : defaultValue);
    }

    public void setDatetime(Object sender, String key, LocalDateTime value) {
        LKeyValue kv = userValues.getOrAdd(this.createKey(sender, key));
        kv.setDatetimeValue(value);
    }

    public LocalDate getDate(Object sender, String key, LocalDate defaultValue) {
        LKeyValue el = get(sender, key);
        return (el != null ? (el.getDateValue() != null ? el.getDateValue() : defaultValue) : defaultValue);
    }

    public void setDate(Object sender, String key, LocalDate value) {
        LKeyValue kv = userValues.getOrAdd(this.createKey(sender, key));
        kv.setDateValue(value);
    }

    public void setDouble(Object sender, String key, Double value) {
        LKeyValue kv = userValues.getOrAdd(this.createKey(sender, key));
        kv.setDoubleValue(value);
    }

    /**
     *
     * @param sender
     * @param key
     * @return the data entry or null
     */
    public LKeyValue get(Object sender, String key) {
        String kvKey = this.createKey(sender, key);        
        LKeyValue el = userValues.get(kvKey);
        if (el == null) {
            el = globalValues.get(kvKey);
        }
        return el;
    }

    public LKeyValue get(String key) {
        return get(null, key);
    }

    /**
     *
     * @param sender
     * @param key
     * @return the data entry. A new value was created, if the entry was not
     * existing before.
     */
    public LKeyValue getOrAdd(Object sender, String key) {    
        LKeyValue kv = get(sender, key);
        if (kv == null) {
            String kvKey = createKey(sender, key);
            kv = new LKeyValue(kvKey);
            userValues.add(kv);
        }
        return kv;
    }

    public boolean getBoolean(Object sender, String key, boolean defaultValue) {
        LKeyValue el = get(sender, key);
        if (el == null) {
            el = new LKeyValue(createKey(sender, key));            
            userValues.add(el);
        }
        if (el.getBooleanValue() == null) {
            el.setBooleanValue(defaultValue);
        }
        return el.getBooleanValue();
    }

    public int getInteger(Object sender, String key, int defaultValue) {
        LKeyValue el = get(sender, key);
        return (el != null ? (el.getIntegerValue() != null ? el.getIntegerValue() : defaultValue) : defaultValue);
    }

    public String getString(Object sender, String key, String defaultValue) {
        LKeyValue el = get(sender, key);
        return (el != null ? (el.getStringValue() != null && !el.getStringValue().isEmpty() ? el.getStringValue() : defaultValue) : defaultValue);
    }

    public ILBounds getBounds(Object sender, String key, ILBounds defaultValue) {
        LKeyValue el = get(sender, key);
        return (el != null ? (el.getBoundsValue() != null ? el.getBoundsValue() : defaultValue) : defaultValue);
    }

    public void setBounds(Object sender, String key, ILBounds value) {
        LKeyValue kv = userValues.getOrAdd(this.createKey(sender, key));
        kv.setBoundsValue(value);
    }

    public double getDouble(Object sender, String key, double defaultValue) {
        LKeyValue el = get(sender, key);
        return (el != null ? (el.getDoubleValue() != null ? el.getDoubleValue() : defaultValue) : defaultValue);
    }

    public LKeyYosos<LKeyValue> getUserValues() {
        return userValues;
    }

    public LKeyYosos<LKeyValue> getGlobalValues() {
        return globalValues;
    }

    public String getUserHomeDirectory() {
        return userHomeDirectory;
    }

    public String getAppDirectory() {
        return appDirectory;
    }


}
