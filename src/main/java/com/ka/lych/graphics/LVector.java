package com.ka.lych.graphics;

/**
 * A 2D vector.
 *
 * @author John Hewson
 */
public final class LVector {

    private final double x, y;

    public LVector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the x magnitude.
     * @return 
     */
    public double getX() {
        return x;
    }

    /**
     * Returns the y magnitude.
     * @return 
     */
    public double getY() {
        return y;
    }

    /**
     * Returns a new vector scaled by both x and y.
     *
     * @param sxy x and y scale
     * @return 
     */
    public LVector scale(double sxy) {
        return new LVector(x * sxy, y * sxy);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
