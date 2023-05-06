package com.ka.lych.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import com.ka.lych.LBase;
import com.ka.lych.list.LKeyYosos;
import com.ka.lych.list.LTYoso;
import com.ka.lych.observable.LBoolean;
import com.ka.lych.observable.LObservable;
import com.ka.lych.observable.LString;
import com.ka.lych.annotation.Id;

/**
 *
 * @author klausahrenberg
 * @param <T>
 */
public class LPath_old<T extends LPath_old<T>> extends LTYoso<T>
        implements ILSupportsOwner {

    private final Boolean DEFAULT_DISABLED = false;
    private final String DEFAULT_DISPLAY_NAME = null;
    private final Path DEFAULT_PATH = null;
    private final String DEFAULT_KEY = null;

    public static DirectoryStream.Filter<Path> FILTER_DIRECTORIES = (Path path) -> LPath_old.isDirectory(path) && !LPath_old.isHidden(path);
    public static DirectoryStream.Filter<Path> FILTER_FILES = (Path path) -> !LPath_old.isDirectory(path) && !LPath_old.isHidden(path);

    private Object owner;
    private LObservable<Path> path;
    private final DirectoryStream.Filter<Path> filter;
    private final Class pathClass;
    @Id
    private LString key;
    
    private LString displayName;
    
    private LBoolean disabled;

    public LPath_old(Path path, Object owner, DirectoryStream.Filter<Path> filter) {
        super();
        setKey(LPath_old.keyOf(path));
        if (getClass() != LPath_old.class) {
            this.pathClass = LReflections.getParameterClass(getClass(), 0);
        } else { 
            this.pathClass = LPath_old.class;
        }
        setPath(path);
        this.owner = owner;
        this.filter = filter;
        /*
        //tbi
        this.observableHasChildrens().setLateLoader(from -> {
            return evaluateHasChildrens();
        });*/
        this.setDisplayName(createDisplayName(getPath()));        
    }

    public LString observableKey() {
        if (key == null) {
            key = new LString(DEFAULT_KEY);
        }
        return key;
    }

    public String getKey() {
        return key != null ? key.get() : DEFAULT_KEY;
    }

    public void setKey(String key) {
        observableKey().set(key);
    }
    
    public LObservable<Path> observablePath() {
        if (path == null) {
            path = new LObservable<>(DEFAULT_PATH);
            path.addListener(change -> {
                if (getChildrens() != null) {
                    //getChildrens().clear();
                    setChildrens(null);
                }
            });
        }
        return path;
    }

    public Path getPath() {
        return path != null ? path.get() : DEFAULT_PATH;
    }

    public void setPath(Path path) {
        observablePath().set(path);
    }    

    public boolean isRoot() {
        return (getPath() == null);
    }

    private String createDisplayName(Path path) {
        Path fileName = (path != null ? path.getFileName() : null);
        String result = (fileName != null ? fileName.toString() : (path != null ? path.toString() : "<na>"));
        if (result.endsWith(FileSystems.getDefault().getSeparator())) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    public LString observableDisplayName() {
        if (displayName == null) {
            displayName = new LString(DEFAULT_DISPLAY_NAME);
            /*displayName.setLateLoader(from -> {
                return createDisplayName(getPath());
            });*/
        }
        return displayName;
    }    

    public String getDisplayName() {
        return displayName != null ? displayName.get() : DEFAULT_DISPLAY_NAME;
    }

    private void setDisplayName(String displayName) {
        observableDisplayName().set(displayName);
    }

    public LBoolean observableDisabled() {
        if (disabled == null) {
            disabled = new LBoolean(DEFAULT_DISABLED);
        }
        return disabled;
    }

    public Boolean isDisabled() {
        return disabled != null ? disabled.get() : DEFAULT_DISABLED;
    }

    private void setDisabled(Boolean disabled) {
        observableDisabled().set(disabled);
    }

    private boolean evaluateHasChildrens() {
        try {            
            DirectoryStream<Path> dirStream = Files.newDirectoryStream(this.getPath(), filter);
            return dirStream.iterator().hasNext();
        } catch (AccessDeniedException ade) {    
            this.setDisabled(true);
            return false;
        } catch (IOException ioe) {
            LLog.error(this, ioe.getLocalizedMessage(), ioe);
            return false;
        }
    }

    @Override
    public boolean loadChildrens() {        
        if ((getChildrens() == null) && ((isRoot()) || (isDirectory()))) {            
            setChildrens(new LKeyYosos<>());
            LFuture.execute(task -> {
                if (isRoot()) {
                    this.createRoots();
                } else {
                    try {
                        DirectoryStream<Path> stream = Files.newDirectoryStream(getPath(), filter);
                        for (Path p : stream) {
                            this.getOrAdd(p);
                        }
                    } catch (IOException ioe) {
                        LLog.error(this, ioe.getLocalizedMessage(), ioe);
                    }
                }
                return null;
            });
            
        }
        return ((getChildrens() != null) && (getChildrens().size() > 0));
    }

    @SuppressWarnings("unchecked")
    public T getOrAdd(Path path) {
        T result = (getChildrens() != null ? this.getChildrens().get(LPath_old.keyOf(path)) : null);
        if (result == null) {
            Object[] initArguments = {path, this.owner, this.filter};
            result = (T) LReflections.newInstance(pathClass, initArguments);
            this.addChildren(result);
        }
        return result;
    }

    protected void createRoots() {
        switch (LBase.getOS()) {
            case WINDOWS -> {
                //home                     
                Path homePath = Path.of(System.getProperty("user.home"));                
                this.getOrAdd(homePath);
                //Drives
                Iterable<Path> it_path = FileSystems.getDefault().getRootDirectories();
                it_path.forEach(path -> {                    
                    this.getOrAdd(path);
                });
                //Pictures Directory
                Path imagePath = Path.of(homePath.toString() + FileSystems.getDefault().getSeparator() + "Pictures");
                if (Files.exists(imagePath)) {
                    this.getOrAdd(imagePath);
                }
            }
            case MAC -> {
                /*//EigeneDateien
                this.getOrAdd(fileSystem.getHomeDirectory());
                //Desktop
                this.getOrAdd(new File(fileSystem.getHomeDirectory().getAbsolutePath() + "/Desktop"));
                //Laufwerke
                for (File r : (new File("/Volumes")).listFiles()) {
                    this.getOrAdd(r);
                }*/
                throw new UnsupportedOperationException("Mac not supported yet");
            }
            case LINUX -> {
                //home                     
                Path homePath = Path.of(System.getProperty("user.home"));                
                this.getOrAdd(homePath);
                //root /
                Iterable<Path> it_path = FileSystems.getDefault().getRootDirectories();
                it_path.forEach(path -> {                    
                    LPath_old p = this.getOrAdd(path);
                    p.setDisplayName(LBase.getResources().localize(owner, "fileSystemRoot"));
                });
                //User-dirs
                Path userDirs = Path.of(homePath.toString() + FileSystems.getDefault().getSeparator() + ".config/user-dirs.dirs");
                if (LPath_old.exists(userDirs)) {
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(userDirs.toString()));
                        String strLine;
                        while ((strLine = br.readLine()) != null) {
                            strLine = strLine.trim();
                            if ((strLine.startsWith("XDG_DESKTOP_DIR")) || (strLine.startsWith("XDG_PICTURES_DIR"))) {
                                Path userPath;
                                if ((userPath = getUserPath(strLine, homePath)) != null) {
                                    getOrAdd(userPath);
                                }
                                System.out.println(userPath);

                            }
                        }

                    } catch (IOException ioe) {
                        LLog.error(this, "Can't read user dirs", ioe);
                    }
                } else {
                    LLog.error(this, "Can't evaluate user dirs at linux system. (Missing file '" + userDirs.toString() + "')");
                }
            }
            default -> {
                Iterable<Path> it_path = FileSystems.getDefault().getRootDirectories();
                it_path.forEach(path -> {
                    this.getOrAdd(path);
                });
            }
        }
    }

    private Path getUserPath(String strLine, Path homeDir) {
        Path result = null;
        int p = strLine.indexOf("=");
        if (p > -1) {
            strLine = strLine.substring(p + 1);
            if ((strLine.charAt(0) == '"') && (strLine.charAt(strLine.length() - 1) == '"')) {
                strLine = strLine.substring(1, strLine.length() - 1);
            }
            if (strLine.startsWith("$HOME")) {
                result = Path.of(homeDir.toString() + strLine.substring(5));
            }
        }
        return ((result != null) && (LPath_old.exists(result)) ? result : null);
    }

    @Override
    public Object getOwner() {
        return this.owner;
    }

    public boolean exists() {
        return LPath_old.exists(getPath());
    }

    public boolean isDirectory() {
        return LPath_old.isDirectory(getPath());
    }

    public boolean isHidden() {
        return LPath_old.isHidden(getPath());
    }

    public static boolean exists(Path path) {
        return Files.exists(path); //, LinkOption.NOFOLLOW_LINKS);
    }

    public static boolean isDirectory(Path path) {
        return Files.isDirectory(path); //, LinkOption.NOFOLLOW_LINKS);
    }

    public static boolean isHidden(Path path) {
        try {
            return Files.isHidden(path);
        } catch (IOException ioe) {
            return true;
        }
    }

    public static String keyOf(Path path) {
        return (path != null ? path.toString() : "null");
    }
        
    

}
