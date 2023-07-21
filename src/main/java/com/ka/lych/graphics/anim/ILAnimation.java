package com.ka.lych.graphics.anim;

import com.ka.lych.graphics.LCanvasRenderer;
import com.ka.lych.graphics.LShape;

/**
 *
 * @author klausahrenberg
 */
public interface ILAnimation {
    
    public Integer getDelay();
    
    public Integer getDuration();
    
    public Boolean isInfinite();
    
    public void execute(LCanvasRenderer canvasRenderer, LShape shape, long now);
    
}
