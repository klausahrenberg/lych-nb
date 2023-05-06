package com.ka.lych.graphics;

import com.ka.lych.annotation.Xml;

/**
 *
 * @author klausahrenberg
 * @param <C>
 * @param <D>
 */
public class LFillAlpha<C, D>
        implements ILCanvasCommand<C, D> {

    double alpha;

    public LFillAlpha(){
        this(1.0);
    }
    
    public LFillAlpha(double alpha) {
        this.alpha = alpha;
    }

    @Override
    public void execute(LCanvasRenderer<C, D> canvasRenderer, long timeLine) {
        canvasRenderer.setFillAlpha(alpha);
    }

    public double getAlpha() {
        return alpha;
    }

    @Xml
    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }
    
    
    
}
