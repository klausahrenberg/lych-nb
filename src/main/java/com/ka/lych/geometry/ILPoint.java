package com.ka.lych.geometry;

import com.ka.lych.observable.LDouble;

/**
 *
 * @author klausahrenberg
 * @param <BC>
 */
public interface ILPoint<BC extends ILPoint> {
    
    public LDouble x();

    public default BC x(double x) {
        x().set(x);
        return (BC) this;
    }
    
    public LDouble y();

    public default BC y(double y) {
        y().set(y);
        return (BC) this;
    }
    
    public default int xIntValue() {
        return (int) Math.round(x().get());
    }
    
    public default int xIntCeil() {
        return (int) Math.ceil(x().get());
    }    

    public default int getXIntFloor() {
        return (int) Math.floor(x().get());
    }

    public default int yIntValue() {
        return (int) Math.round(y().get());
    }
    
    public default int getYIntCeil() {
        return (int) Math.ceil(y().get());
    }

    public default int getYIntFloor() {
        return (int) Math.floor(y().get());
    }
    
    public default BC point(double x, double y) {
        return (BC) x(x).y(y);
    }
    
}
