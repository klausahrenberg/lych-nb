package com.ka.lych.observable;

/**
 *
 * @author klausahrenberg
 */
public class LText extends LString {

    public LText() {        
        this.initialize();
    }

    public LText(String initialValue) {
        super(initialValue);
        this.initialize();
    }
    
    private void initialize() {
        //this.loadingState = LLoadingState.NOT_LOADED;
    }

    /*@Override
    public boolean isLoadable() {
        //CBLOB field in database, should not be in a select or group by statement 
        //and shopuld be loaded separately
        return true;
    }*/
    
}
