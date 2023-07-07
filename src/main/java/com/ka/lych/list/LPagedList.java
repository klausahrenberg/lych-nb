package com.ka.lych.list;

import java.util.Collection;

public class LPagedList<T> extends LYosos<T> {

    public LPagedList(int size) {
        super();  
        allowNullElements = true;
        for (int i = 0; i < size; i++) {
            this.add(null);
        }
    }

    public boolean setAll(int index, Collection<? extends T> c) {
        for (T yoso : c) {
            set(index, yoso);
            index++;
        }
        return true;
    }        
    
}
