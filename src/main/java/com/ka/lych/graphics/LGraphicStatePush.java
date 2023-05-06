package com.ka.lych.graphics;

/**
 *
 * @author klausahrenberg
 * @param <C>
 */
public class LGraphicStatePush<C, D>
        implements ILCanvasCommand<C, D> {

    protected static LGraphicStatePush pushGraphicState;

    @Override
    public void execute(LCanvasRenderer<C, D> canvasRenderer, long timeLine) {
        canvasRenderer.pushCanvasState();
    }

    public static LGraphicStatePush getInstance() {
        if (pushGraphicState == null) {
            pushGraphicState = new LGraphicStatePush();
        }
        return pushGraphicState;
    }
    
    @Override
    public String toString() {
        return getClass().getName() + " []";
    }

}
