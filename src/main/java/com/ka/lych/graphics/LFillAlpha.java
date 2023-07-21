package com.ka.lych.graphics;

import com.ka.lych.annotation.Json;


/**
 *
 * @author klausahrenberg
 * @param <C>
 * @param <D>
 */
public class LFillAlpha<C, D>
        implements ILCanvasCommand<C, D> {
    
    @Json
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

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }
    
    
    
}
