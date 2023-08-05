package com.ka.lych.geometry;

import com.ka.lych.observable.LDouble;

/**
 *
 * @author klausahrenberg
 * @param <BC>
 */
public interface ILSize<BC extends ILSize> {
    
    public LDouble width();

    public default BC width(double width) {
        width().set(width);
        return (BC) this;
    }
    
    public LDouble height();

    public default BC height(double height) {
        height().set(height);
        return (BC) this;
    }
    
    public default BC size(double width, double height) {
        return (BC) width(width).height(height);
    }
    
    public boolean isEmpty();
    
}
