package com.ka.lych.ui;

/**
 *
 * @author klausahrenberg 
 * @param <BC> Component class
 */
public interface ILPane<BC> extends ILControl<BC>, ILChildrensIterable<BC> {        

    public void buildView();
    
    public void initialize();
    
}
