package com.ka.lych.test;

import com.ka.lych.geometry.LBounds;
import com.ka.lych.graphics.LRectangle;
import com.ka.lych.graphics.LCanvas;
import com.ka.lych.graphics.LColor;

/**
 *
 * @author klausahrenberg
 */
public class LTestCanvas extends LCanvas {    
        
    public LTestCanvas(LColor fillColor) {
        //setRotation(60);
        //setScaleFactor(1.50);
        //setScaleMode(LCanvas.SCALEMODE_FITWIDTH);
        this.setViewBounds(new LBounds(0, 0, 500, 400));
        //this.add(new LSolidPaint(fillColor));
        this.add(new LRectangle(20, 20, 100, 50));
    }

    @Override
    public void createPath() {
        
        
        
        
        //lp.setFillColor(fillColor);
        //LEllipse el = new LEllipse(100, 100, 300, 200);
        //this.addShape(el, LColor.BLACK, fillColor);
        
        /*this.addCommand(LGraphicStateCommand.getPushInstance());
        this.addCommand(LGraphicStateCommand.getPushInstance());
        
        LMatrix test = new LMatrix();
        
        //
        test.translate(-100, 400);
        test.rotate(Math.toRadians(30));
        test.scale(0.5, 0.5);
        test.scale(5, 5);
        
        this.addCommand(new LMatrixCommand(test));
        
        this.addCommand(LGraphicStateCommand.getPopInstance());
        this.addCommand(LGraphicStateCommand.getPopInstance());
        
        
        lp = new LPaint();
        lp.setAntiAlias(true);
        lp.setStrokeWidth(10);
        lp.setFillColor(LColor.YELLOW);
        this.draw(new LEllipse(500, 100, 300, 200), lp);*/
        
    }
    
}
