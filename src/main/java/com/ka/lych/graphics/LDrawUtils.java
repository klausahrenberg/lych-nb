package com.ka.lych.graphics;

import com.ka.lych.geometry.ILBounds;

/**
 *
 * @author klausahrenberg
 */
public class LDrawUtils {
    
    public static double getX(ILBounds bounds, double percent) {
        return percent * bounds.getWidth() + bounds.getX();
    }
    
    public static double getY(ILBounds bounds, double percent) {
        return percent * bounds.getHeight() + bounds.getY();
    }
    
    public static double getWidth(ILBounds bounds, double percent) {
        return percent * bounds.getWidth();
    }
    
    public static double getHeight(ILBounds bounds, double percent) {
        return percent * bounds.getHeight();
    }
    
    public static LColor mixColor(LColor c1, LColor c2) {
        return new LColor((c1.getRed()+c2.getRed())/2,
                          (c1.getGreen()+c2.getGreen())/2,
                          (c1.getBlue()+c2.getBlue())/2);        
    }
    
    public static LColor fadeColor(LColor c, float fade) {        
        int offset = Math.round(255 * fade - 255);
        int r = c.getRed() + offset;
        int g = c.getGreen() + offset;
        int b = c.getBlue() + offset;
        r = (r > 255 ? 255 : r);
        g = (g > 255 ? 255 : g);
        b = (b > 255 ? 255 : b);
        r = (r < 0 ? 0 : r);
        g = (g < 0 ? 0 : g);
        b = (b < 0 ? 0 : b);
        return new LColor(r, g, b);   
    }
    
}
