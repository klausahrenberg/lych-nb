package com.ka.lych.util;

import com.ka.lych.LBase;
import com.ka.lych.observable.LString;
import static com.ka.lych.util.LOS.WINDOWS;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 *
 * @author klausahrenberg
 */
public class LJsoperties {

    protected final String userHomeDirectory;

    public LJsoperties(String appName) {
        String uh = System.getProperties().getProperty("user.home");

        switch (LBase.getOS()) {
            case MAC ->
                userHomeDirectory = uh + File.separator + "Library" + File.separator + "Application Support" + File.separator + appName;
            case WINDOWS ->
                userHomeDirectory = uh + File.separator + "AppData" + File.separator + "Roaming" + File.separator + appName;
            default ->
                userHomeDirectory = uh + File.separator + ".config" + File.separator + appName;
        }
        this.checkIfDirectoryExists(userHomeDirectory);
    }

    protected static void checkIfDirectoryExists(String dirName) {
        File f = new File(dirName);
        if (!f.exists()) {
            try {
                if (!f.mkdir()) {
                    throw new Exception("mkdir failed (" + dirName + ")");
                }
            } catch (Exception e) {
                LLog.error(LJsoperties.class, "Can't create folder '" + dirName + "'", e, true);
            }
        }
    }
    
    public void load(Object o) {
        load(o, null, true);
    }

    public void load(Object o, String id, boolean copyTemplate) {
        try {
            var fileName = o.getClass().getSimpleName() + (!LString.isEmpty(id) ? ILConstants.DOT + id : "");
            File jsonFile = new File(userHomeDirectory + File.separator + fileName + ILConstants.DOT + ILConstants.KEYWORD_FILE_JSON_SUFFIX);
            if (!jsonFile.exists()) {
                if (copyTemplate) {
                    File tempFile = null;
                    String templateName = o.getClass().getPackageName()
                            .replaceAll("\\.", ILConstants.SLASH)
                            .concat(ILConstants.SLASH)
                            .concat(fileName)
                            .concat(ILConstants.DOT)
                            .concat(ILConstants.KEYWORD_FILE_JSON_SUFFIX);
                    var resStream = getClass().getClassLoader().getResourceAsStream(templateName);                    
                    if (resStream == null) {
                        LLog.debug(this, "Template of json settings '%s' doesn't exists. Can't copy it to directory '%s'.", templateName, jsonFile.getAbsolutePath());
                    } else {
                        LJsonParser.update(o).inputStream(resStream).parse();
                        this.save(o, id);
                        LLog.debug(this, "Settings loaded from template: '%s'. Settings file with default values created at: '%s'.", templateName, jsonFile.getAbsolutePath());
                    }                                        
                }
            } else {
                LJsonParser.update(o).file(jsonFile).parse();
                //LJsonParser.update(o, jsonFile);
                
                LLog.debug(this, "Settings with new loading from file: '%s'.", jsonFile.getAbsolutePath());
            }
        } catch (Exception ex) {
            //jsonFile = null;
            LLog.error(this, ex.getMessage(), ex);
        }
    }
    
    public void save(Object o, String id) {
        try {
            var fileName = o.getClass().getSimpleName() + (!LString.isEmpty(id) ? ILConstants.DOT + id : "");
            File jsonFile = new File(userHomeDirectory + File.separator + fileName + ILConstants.DOT + ILConstants.KEYWORD_FILE_JSON_SUFFIX);
            
            BufferedWriter writer = new BufferedWriter(new FileWriter(jsonFile));
            writer.write(LJson.of(o).toString());
            writer.close();
        } catch (Exception ex) {
            LLog.error(this, ex.getMessage(), ex);
        }
    }

    @Deprecated
    public String getString(Object sender, String key, String defaultValue) {
        throw new UnsupportedOperationException("Not supported anymore. Use @LJSON declaration in your class for settings.");
    }

    @Deprecated
    public void setString(Object sender, String key, String value) {
        throw new UnsupportedOperationException("Not supported anymore. Use @LJSON declaration in your class for settings.");
    }

}
