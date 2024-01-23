package com.ka.lych.util;

import com.ka.lych.LBase;
import com.ka.lych.exception.LUnchecked;
import com.ka.lych.list.LList;
import com.ka.lych.observable.LString;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 *
 * @author klausahrenberg
 */
public record LPath(Path path, LString displayName) {
    
    public static String FILETYPE_EXCEL = ".xlsx";
    public static String FILETYPE_EXCEL_MACRO = ".xlsm";

    private static LPath root;

    public static LPath of(Path path) {
        return new LPath(path, new LString(path.toString()));
    }

    public static LPath root() {
        if (root == null) {
            root = new LPath(null, new LString("<root>"));
        }
        return root;
    }

    public static boolean isRoot(LPath path) {
        return (path.path() == null);
    }
    
    public boolean isFiletype(String filetype) {
        return isFiletype(this, filetype);
    }
    
    public static boolean isFiletype(LPath path, String filetype) {
        return path.path().toString().toLowerCase().endsWith(filetype.toLowerCase());
    }
    
    public boolean isFiletypeExcel() {
        return isFiletypeExcel(this);
    }
    
    public FileTime lastModifiedTime() {
        return lastModifiedTime(this);
    }
    
    public static FileTime lastModifiedTime(LPath path) {
        try {
            return Files.getLastModifiedTime(path.path());
        } catch (IOException ioe) {
            return null;
        }
    }
    
    public static boolean isFiletypeExcel(LPath path) {
        return ((isFiletype(path, FILETYPE_EXCEL)) || (isFiletype(path, FILETYPE_EXCEL_MACRO)));
    }

    public boolean isDirectory() {
        return LPath.isDirectory(this);
    }

    public static boolean isDirectory(LPath path) {
        return (!isRoot(path) ? Files.isDirectory(path.path()) : true);
    }

    public LPath getParent() {
        return LPath.getParent(this);
    }

    public static LPath getParent(LPath path) {
        return (isRoot(path) ? path : LPath.of(path.path().getParent()));
    }

    public static LList<LPath> createRoots() {
        var result = new LList<LPath>();
        switch (LBase.getOS()) {
            case WINDOWS -> {
                //home                     
                Path homePath = Path.of(System.getProperty("user.home"));
                result.add(LPath.of(homePath));
                //Drives
                Iterable<Path> it_path = FileSystems.getDefault().getRootDirectories();
                it_path.forEach(path -> result.add(LPath.of(path)));
                //Pictures Directory
                Path imagePath = Path.of(homePath.toString() + FileSystems.getDefault().getSeparator() + "Pictures");
                if (Files.exists(imagePath)) {
                    result.add(LPath.of(imagePath));
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
                result.add(LPath.of(homePath));
                //root /
                Iterable<Path> it_path = FileSystems.getDefault().getRootDirectories();
                it_path.forEach(path -> result.add(LPath.of(path)));
                //User-dirs
                Path userDirs = Path.of(homePath.toString() + FileSystems.getDefault().getSeparator() + ".config/user-dirs.dirs");
                if (LPath.exists(userDirs)) {
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(userDirs.toString()));
                        String strLine;
                        while ((strLine = br.readLine()) != null) {
                            strLine = strLine.trim();
                            if ((strLine.startsWith("XDG_DESKTOP_DIR")) || (strLine.startsWith("XDG_PICTURES_DIR"))) {
                                Path userPath;
                                if ((userPath = getUserPath(strLine, homePath)) != null) {
                                    result.add(LPath.of(userPath));
                                }
                            }
                        }
                    } catch (IOException ioe) {
                        throw new LUnchecked(ioe);
                    }
                } else {
                    throw new LUnchecked("Can't evaluate user dirs at linux system. (Missing file '%s')", userDirs.toString());
                }
            }
            default -> {
                Iterable<Path> it_path = FileSystems.getDefault().getRootDirectories();
                it_path.forEach(path -> {
                    result.add(LPath.of(path));
                });
            }
        }
        return result;
    }

    private static Path getUserPath(String strLine, Path homeDir) {
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
        return ((result != null) && (LPath.exists(result)) ? result : null);
    }

    public static LFuture<LList<LPath>, IOException> fetch(LPath path, Optional<Filter<? super Path>> filter) {
        Objects.requireNonNull(path, "Path can't be null");
        return LFuture.execute(runnable -> {
            if (isRoot(path)) {
                return createRoots();
            } else {
                var result = new LList<LPath>();
                var stream = (filter.isPresent() ? Files.newDirectoryStream(path.path(), filter.get()) : Files.newDirectoryStream(path.path()));
                stream.forEach(p -> result.add(LPath.of(p)));
                return result;
            }
        });
    }

    /*public static LList<LPath> fetch(LPath path, Optional<Filter<? super Path>> filter) throws IOException {
        Objects.requireNonNull(path, "Path can't be null");

        if (isRoot(path)) {
            return createRoots();
        } else {
            var result = new LList<LPath>();
            var stream = (filter.isPresent() ? Files.newDirectoryStream(path.path(), filter.get()) : Files.newDirectoryStream(path.path()));
            stream.forEach(p -> result.add(LPath.of(p)));
            return result;
        }
    }*/
    public boolean exists() {
        return LPath.exists(this.path());
    }

    public static boolean exists(Path path) {
        return Files.exists(path); //, LinkOption.NOFOLLOW_LINKS);
    }

    public static class LPathGlobFilter
            implements DirectoryStream.Filter<Path> {

        private final Pattern pattern;

        public LPathGlobFilter(String glob) {
            String regexPattern = LGlobs.toUnixRegexPattern(glob);
            pattern = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE);
        }

        @Override
        public boolean accept(Path entry) throws IOException {
            return pattern.matcher(entry.getFileName().toString()).matches();
        }

    }

}
