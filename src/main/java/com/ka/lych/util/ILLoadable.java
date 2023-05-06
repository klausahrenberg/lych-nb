/*
 * Interface to declare that loading of content/data is supported. For time consuming loading process of data
 */
package com.ka.lych.util;

/**
 *
 * @author klausahrenberg
 * @param <E>
 */
public interface ILLoadable<E extends Throwable> {
    
    /**
     * Indicates, if data will be loaded at request or they are available right now
     * @return true, if loading on request, check via getLoadingState() the current status of data. 
     * false, if data are just there
     */    
    public boolean isLoadable();
    
    public LLoadingState getLoadingState();    
    
    //public LWaiter<E> load();
    public void load() throws E;
    
    default public boolean isNotLoaded() {
        return this.getLoadingState() == LLoadingState.NOT_LOADED;
    }
    
    default public boolean isLoading() {
        return this.getLoadingState() == LLoadingState.LOADING;
    }
    
    default public boolean isLoaded() {
        return this.getLoadingState() == LLoadingState.LOADED;
    }
    
}
