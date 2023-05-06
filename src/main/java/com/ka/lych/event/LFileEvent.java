package com.ka.lych.event;

import java.io.File;
import java.util.List;

/**
 *
 * @author klausahrenberg
 */
public class LFileEvent extends LEvent {
    
    private final List<File> files;
    private final int index;
    
    @SuppressWarnings("unchecked")
    public LFileEvent(Object source, List<File> files, int index) {
        super(source);
        this.files = files;        
        this.index = index;
    }

    public List<File> getFiles() {
        return files;
    } 

    public int getIndex() {
        return index;
    }
        
}
