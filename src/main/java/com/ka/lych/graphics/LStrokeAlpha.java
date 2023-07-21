package com.ka.lych.graphics;

/**
 *
 * @author klausahrenberg
 * @param <C>
 */
public class LStrokeAlpha<C, D>
        implements ILCanvasCommand<C, D> {

    double alpha;

    public LStrokeAlpha(double alpha) {
        this.alpha = alpha;
    }

    @Override
    public void execute(LCanvasRenderer<C, D> canvasRenderer, long timeLine) {
        canvasRenderer.setStrokeAlpha(alpha);
    }
}
