package com.ka.lych.graphics;

import com.ka.lych.geometry.LInsets;
import com.ka.lych.geometry.LSize;
import com.ka.lych.geometry.LBounds;
import com.ka.lych.event.LChangedEvent;
import com.ka.lych.event.LEventHandler;
import com.ka.lych.geometry.LScaleMode;
import com.ka.lych.list.LMap;
import com.ka.lych.observable.*;
import com.ka.lych.util.*;

/**
 *
 * @author klausahrenberg
 * @param <C> native Canvas class
 * @param <D> native Color class
 */
public abstract class LCanvasRenderer<C, D>
        implements ILRunnable<LCanvas, LException>, ILConstants {

    private final LCanvas DEFAULT_CANVAS = null;

    protected LObservable<LCanvas> canvas;
    protected LObservable<LBounds> paintBounds;
    private LEventHandler<LChangedEvent> onChanged;
    protected boolean needsNewRendering;
    protected int renderPriority;
    private LObservable<LScaleMode> scaleMode;
    private LDouble scaleFactor;
    protected LShape lastShape;
    final protected LObservable<LSize> availableSize;
    protected LObservable<LInsets> insets;
    protected boolean runInThread;
    protected final LMap<LAbstractPaint, D> paintsCache;
    private LObservable<LCanvasColorSpace> colorSpace;
    private LTask service;
    private boolean adjustingScale;
    private final LLoadingService loadingService;

    protected ILHandler<LChangedEvent> canvasChangedEvent = event -> {
        needsNewRendering = true;
        notifyOnChanged(event);
    };

    protected ILChangeListener<Double> canvasRotationListener = oldValue -> {
        needsNewRendering = true;
        updatePaintBounds();
    };

    public LCanvasRenderer(LCanvas canvas) {
        adjustingScale = false;
        renderPriority = Thread.NORM_PRIORITY;
        paintsCache = new LMap<>();
        needsNewRendering = false;
        insets = new LObservable<>(new LInsets(5, 5, 5, 5));
        insets.addListener(listener -> updatePaintBounds());
        this.runInThread = true;
        availableSize = new LObservable<>(new LSize(0, 0));
        availableSize.addListener(oldValue -> updatePaintBounds());
        paintBounds = new LObservable<>(new LBounds());
        paintBounds.addListener(ce -> needsNewRendering = true);
        loadingService = new LLoadingService();
        setCanvas(canvas);
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void finalize() throws Throwable {
        this.cancelRendering();
        super.finalize();
    }

    public final LEventHandler<LChangedEvent> onChanged() {
        if (onChanged == null) {
            onChanged = new LEventHandler<>();
        }
        return onChanged;
    }

    public final void addOnChanged(ILHandler<LChangedEvent> value) {
        onChanged().add(value);
    }

    public final void removeOnChanged(ILHandler<LChangedEvent> value) {
        onChanged().remove(value);
    }

    public final ILHandler<LChangedEvent> getOnChanged() {
        return (onChanged != null ? onChanged().get() : null);
    }

    private synchronized void notifyOnChanged(LChangedEvent changedEvent) {
        if (onChanged != null) {
            onChanged.fireEvent(changedEvent);
        }
    }

    public LObservable<LBounds> paintBounds() {
        return paintBounds;
    }

    public LBounds getPaintBounds() {
        return paintBounds.get();
    }

    public LObservable<LCanvas> canvas() {
        if (canvas == null) {
            canvas = new LObservable<>(DEFAULT_CANVAS);
            canvas.addListener(change -> {
                cancelRendering();
                if (change.oldValue() != null) {
                    change.oldValue().onChanged().remove(canvasChangedEvent);
                    //oldValue.observableState().removeListener(canvasStateListener);
                    change.oldValue().rotation().removeListener(canvasRotationListener);
                }
                if (getCanvas() != null) {
                    getCanvas().rotation().addListener(canvasRotationListener);
                    getCanvas().onChanged().add(canvasChangedEvent);
                    //getCanvas().observableState().addListener(canvasStateListener);
                }
                paintsCache.clear();
                needsNewRendering = true;
                updatePaintBounds();
            });
        }
        return canvas;
    }

    public LCanvas getCanvas() {
        return canvas != null ? canvas.get() : DEFAULT_CANVAS;
    }

    public void setCanvas(LCanvas canvas) {
        canvas().set(canvas);
    }
    
    public int getRenderPriority() {
        return renderPriority;
    }

    public void setRenderPriority(int renderPriority) {
        this.renderPriority = renderPriority;
    }

    public void setLastShape(LShape shape) {
        this.lastShape = shape;
    }

    public LShape getLastShape() {
        return lastShape;
    }

    public LObservable<LScaleMode> scaleMode() {
        if (scaleMode == null) {
            scaleMode = new LObservable<>(LScaleMode.FIT_PAGE);
            scaleMode.addListener(listener -> updatePaintBounds());
        }
        return scaleMode;
    }

    public LScaleMode getScaleMode() {
        return (scaleMode != null ? scaleMode.get() : LScaleMode.FIT_PAGE);
    }

    public void setScaleMode(LScaleMode scaleMode) {
        scaleMode().set(scaleMode);
    }

    public LDouble scaleFactor() {
        if (scaleFactor == null) {
            scaleFactor = new LDouble(1.0);
            scaleFactor.addListener(listener -> updatePaintBounds());
        }
        return scaleFactor;
    }

    public double getScaleFactor() {
        return (scaleFactor != null ? scaleFactor.get() : 1.0);
    }

    public void setScaleFactor(double scaleFactor) {
        scaleFactor().set(scaleFactor);
    }

    public LObservable<LSize> availableSize() {
        return availableSize;
    }

    public LSize getAvailableSize() {
        return availableSize.get();
    }

    /**
     * Set the availableSize for the renderer. If one value changed, a new
     * rendering will be started
     *
     * @param availableWidth
     * @param availableHeight
     */
    public void setAvailableSize(double availableWidth, double availableHeight) {
        getAvailableSize().setSize(availableWidth, availableHeight);
    }

    public LObservable<LInsets> insets() {
        return insets;
    }

    public LInsets getInsets() {
        return insets.get();
    }

    public void setInsets(LInsets insets) {
        insets().set(insets);
    }

    private boolean updatePaintBounds() {
        if ((!adjustingScale) && (getCanvas() != null) && (availableSize != null) && (insets != null)) {
            double w, h;
            double availableWidth = getAvailableSize().getWidth() - (getInsets().getLeft() + getInsets().getRight());
            if ((getScaleMode() == LScaleMode.FIT_WIDTH) || (getScaleMode() == LScaleMode.FIT_PAGE)) {
                LMatrix matrix = getCanvas().getInitialMatrix(LScaleMode.FACTOR, 1.0, null, false, null);
                LBounds vB = getCanvas().getViewBoundsTransformed(matrix);
                double availableHeight = getAvailableSize().getHeight() - (getInsets().getTop() + getInsets().getBottom());
                double newScaleFactor = availableWidth / vB.getWidth();
                w = availableWidth;
                h = vB.getHeight() * newScaleFactor;
                if ((getScaleMode() == LScaleMode.FIT_PAGE) && (h > availableHeight)) {
                    newScaleFactor = availableHeight / h;
                    w = w * newScaleFactor;
                    h = availableHeight;
                    //Correct to final scaleFactor
                    newScaleFactor = w / vB.getWidth();
                }
                adjustingScale = true;
                scaleFactor().set(newScaleFactor);
                adjustingScale = false;
            } else {
                LMatrix matrix = getCanvas().getInitialMatrix(getScaleMode(), getScaleFactor(), null, false, null);
                LBounds vB = getCanvas().getViewBoundsTransformed(matrix);
                w = vB.getWidth();
                h = vB.getHeight();

            }
            double x = getInsets().getLeft() + (getScaleMode() == LScaleMode.FACTOR ? 0.0 : (availableWidth - w) / 2.0);
            getPaintBounds().setBounds(x, getInsets().getTop(), w, h);
        }
        return needsNewRendering;
    }

    public synchronized void update() {
        if ((getCanvas() != null) && (getCanvas().getState() == LCanvasState.NOT_PARSED)) {            
            if (!(getCanvas() instanceof ILLoadable)) {
                throw new IllegalStateException("Canvas is not parseable (ILParseable not implemented) and state is 'not parsed'");
            }                        
            loadingService.addLoadable((ILLoadable) getCanvas(), renderPriority);           
        }
         LLog.test(this, "np %s %s %s", (needsNewRendering), (getPaintBounds().getWidth() > 0), (getPaintBounds().getHeight() > 0));
        if ((needsNewRendering) && (getPaintBounds().getWidth() > 0) && (getPaintBounds().getHeight() > 0)) {            
            needsNewRendering = false;
            cancelRendering();
            if (runInThread) {
                if (isHandlingAnimations()) {
                    var canvasAnim = getCanvas().checkAnimations();
                    LFuture.execute(this, 0, canvasAnim.duration(), canvasAnim.infinite());
                } else {
                    LFuture.execute(this);
                }
            } else {                
                run(null);
            }
        }
        //} 
    }

    public boolean isNeedsNewRendering() {
        return needsNewRendering;
    }

    public boolean isRendering() {
        return LFuture.isExecuting(this);
    }

    public synchronized void cancelRendering() {
        LFuture.cancel(this);
        cleanup();
    }

    protected void cleanup() {
        lastShape = null;
    }

    @SuppressWarnings("unchecked")
    public LObservable<LCanvasColorSpace> colorSpace() {
        if (colorSpace == null) {
            colorSpace = new LObservable(LCanvasColorSpace.ORIGINAL);
            colorSpace.addListener(listener -> {
                paintsCache.clear();

            });
        }
        return colorSpace;
    }

    public LCanvasColorSpace getColorSpace() {
        return (colorSpace != null ? colorSpace.get() : LCanvasColorSpace.ORIGINAL);
    }

    public void setGrayScale(LCanvasColorSpace colorSpace) {
        colorSpace().set(colorSpace);
    }

    /**
     * This function tells LCanvasRenderer, if possible animations should
     * handled by rendering or not.
     *
     * @return true, if LCanvasRenderer should do the timing and cycling of
     * animations (rendering will be repetead automaticly false, if animations
     * are handled by the native canvas itself
     */
    protected abstract boolean isHandlingAnimations();

    public abstract void startRendering();

    public abstract void setMatrix(LMatrix matrix, boolean concatenate);

    protected abstract void clip(LShape clipArea);

    public abstract void popCanvasState();

    public abstract void pushCanvasState();

    public abstract void setFillAlpha(double fillAlpha);

    public abstract void setStrokeAlpha(double strokeAlpha);

    public abstract void setStroke(LStroke stroke);

    public abstract void setPaint(LAbstractPaint paint);

    public abstract void setBlendMode(LBlendMode blendMode);

    public abstract void drawImage(LRasterImage image);

    public abstract void fillShape(LShape s);

    public abstract void strokeShape(LShape s);

    public abstract void drawTextShape(LTextShape text);

    public LMatrix getInitialMatrix() {
        boolean translatePaintBoundsLocation = true;
        return getCanvas().getInitialMatrix(getScaleMode(), getScaleFactor(), getPaintBounds(), translatePaintBoundsLocation, null);
    }

    @Override
    public LCanvas run(LTask<LCanvas, LException> task) {
        if (paintBounds == null) {
            throw new IllegalArgumentException("PaintBounds can't be null");
        }
        startRendering();
        LMatrix initialMatrix = getInitialMatrix();
        setMatrix(initialMatrix, false);
        @SuppressWarnings("unchecked")
        LChangedEvent changedEvent = (getOnChanged() != null ? new LChangedEvent(LCanvasRenderer.this) : null);
        this.service = service;
        getCanvas().execute(this, System.currentTimeMillis());
        if (!isRenderingCancelled()) {
            notifyOnChanged(changedEvent);
        }
        this.service = null;
        cleanup();
        if ((service != null) && (!service.isCancelled())) {
            notifyOnChanged(changedEvent);
        }
        return getCanvas();
    }

    public boolean isRenderingCancelled() {
        return ((service != null) && (service.isCancelled()));
    }

}
