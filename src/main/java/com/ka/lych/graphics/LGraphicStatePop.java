package com.ka.lych.graphics;

/**
 *
 * @author klausahrenberg
 * @param <C>
 */
public class LGraphicStatePop<C, D>
        implements ILCanvasCommand<C, D> {

    protected static LGraphicStatePop popGraphicState;
    
    @Override
    public void execute(LCanvasRenderer<C, D> canvasRenderer, long timeLine) {
        canvasRenderer.popCanvasState();

    }

    public static LGraphicStatePop getInstance() {
        if (popGraphicState == null) {
            popGraphicState = new LGraphicStatePop();
        }
        return popGraphicState;
    }
    
    @Override
    public String toString() {
        return getClass().getName() + " []";
    }

}
