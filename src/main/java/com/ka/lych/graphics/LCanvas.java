package com.ka.lych.graphics;

import com.ka.lych.geometry.LPoint;
import com.ka.lych.geometry.LGeomUtils;
import com.ka.lych.geometry.ILBounds;
import com.ka.lych.geometry.LBounds;
import com.ka.lych.xml.ILXmlSupport;
import com.ka.lych.xml.LXmlUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ResourceBundle;
import javax.xml.parsers.*;
import com.ka.lych.event.LChangedEvent;
import com.ka.lych.event.LEventHandler;
import com.ka.lych.geometry.ILPoint;
import com.ka.lych.geometry.LScaleMode;
import com.ka.lych.graphics.anim.ILAnimation;
import com.ka.lych.list.LYosos;
import com.ka.lych.list.LYososIterator;
import com.ka.lych.observable.*;
import com.ka.lych.util.*;
import com.ka.lych.util.LReflections.LMethod;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import com.ka.lych.xml.LXmlUtils.LXmlParseInfo;
import com.ka.lych.annotation.Xml;

/**
 *
 * @author klausahrenberg
 */
public class LCanvas extends LShape
        implements ILXmlSupport, ILCloneable {

    @Xml
    protected LObservable<LBounds> viewBounds;
    @Xml
    protected LDouble rotation;
    @Xml
    protected LInteger rotationSnap;
    @Xml
    protected LBoolean rotationEnabled;
    @Xml
    protected LBoolean scaleRotated;
    @Xml
    protected LBoolean preserveRatio;
    @Xml
    protected LObservable<LColor> background;
    @Xml
    private LYosos<LCanvas> marks;
    @Xml
    protected LObservable<LCanvasState> state;
    @Xml
    protected LObservable<LYosos<ILCanvasCommand>> commands;

    protected ILCanvasFontRenderer fontRenderer;
    
    private LEventHandler<LChangedEvent> onChanged;

    public LCanvas() {
        this(500, 300);
    }

    public void initialize(URL location, ResourceBundle resources) {
    }

    public LCanvas(double viewBoundsWidth, double viewBoundsHeight) {
        viewBounds = new LObservable<>(new LBounds(0, 0, viewBoundsWidth, viewBoundsHeight));
    }

    private final ILChangeListener<Double> rotationListener = change -> {
        if (marks != null) {
            for (LCanvas mark : marks) {
                mark.setRotation(mark.getRotation() + this.getRotation() - change.getOldValue().doubleValue());
            }
        }
    };

    /**
     *
     * @param scaleMode
     * @param scaleFactor
     * @param paintBounds - bounds where the canvas will painted inside
     * @param translatePaintBoundsLocation - if true, the initialMatrix will be
     * translated by x, y location of paintBounds
     * @param defaultMatrix
     * @return
     */
    public LMatrix getInitialMatrix(LScaleMode scaleMode, double scaleFactor, LBounds paintBounds, boolean translatePaintBoundsLocation, LMatrix defaultMatrix) {
        LMatrix m = (defaultMatrix == null ? new LMatrix() : defaultMatrix);
        //Create a test matrix to get the x,y offset coordinates
        LMatrix cm = (LMatrix) m.clone();
        if ((paintBounds != null) || (isScaleRotated())) {
            cm.rotate(Math.toRadians(getRotation()));
        }
        if (!LGeomUtils.isEqual(scaleFactor, 1.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION)) {
            cm.scale(scaleFactor, scaleFactor);
        }
        LBounds s = getViewBoundsTransformed(cm);
        //1. Correct the x,y position depending on the result of scaled and rotated matrix
        double translateX = -s.getX();
        double translateY = -s.getY();
        if (paintBounds != null) {
            //2. If paintbounds there, correct the x,y position
            if (translatePaintBoundsLocation) {
                translateX += paintBounds.getX();
                translateY += paintBounds.getY();
            }
            //3. if the paintbounds don't fit to the resulting space from test matrix, center the canvas inside the paintbounds
            switch (scaleMode) {
                case FIT_PAGE:
                    if (!LGeomUtils.isEqual(paintBounds.getHeight(), s.getHeight(), LGeomUtils.DEFAULT_DOUBLE_PRECISION)) {
                        translateY += (paintBounds.getHeight() / 2 - s.getHeight() / 2);
                    }
                // no break
                case FIT_WIDTH:
                    if (!LGeomUtils.isEqual(paintBounds.getWidth(), s.getWidth(), LGeomUtils.DEFAULT_DOUBLE_PRECISION)) {
                        translateX += (paintBounds.getWidth() / 2 - s.getWidth() / 2);
                    }
                    break;
                case FACTOR:
            }
        }
        //m.translate(-s.getX() - translateX, -s.getY() - translateY);
        m.translate(translateX, translateY);
        //And the rotate and scale
        if ((paintBounds != null) || (isScaleRotated())) {
            m.rotate(Math.toRadians(getRotation()));
        }
        if (!LGeomUtils.isEqual(scaleFactor, 1.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION)) {
            m.scale(scaleFactor, scaleFactor);
        }
        return m;
    }

    public LBounds getViewBoundsTransformed(LMatrix matrix) {
        return getRectangleTransformed(matrix, getViewBounds());
    }

    public LBounds getShapeTransformed(LMatrix matrix, LShape shape) {
        if (!shape.isRotatable()) {
            matrix.rotate(Math.toRadians(-this.getRotation()), shape.getX() + shape.getWidth() / 2, shape.getY() + shape.getHeight() / 2);
        }
        LBounds b = getRectangleTransformed(matrix, shape);
        if (!shape.isRotatable()) {
            matrix.rotate(Math.toRadians(this.getRotation()), shape.getX() + shape.getWidth() / 2, shape.getY() + shape.getHeight() / 2);
        }
        return b;
    }

    private LBounds getRectangleTransformed(LMatrix matrix, ILBounds rect) {
        LPoint p = matrix.transform(rect.getX(), rect.getY(), null);
        double x1 = p.getX(), x2 = p.getX();
        double y1 = p.getY(), y2 = p.getY();
        p = matrix.transform(rect.getX() + rect.getWidth(), rect.getY(), null);
        x1 = (p.getX() < x1 ? p.getX() : x1);
        y1 = (p.getY() < y1 ? p.getY() : y1);
        x2 = (p.getX() > x2 ? p.getX() : x2);
        y2 = (p.getY() > y2 ? p.getY() : y2);
        p = matrix.transform(rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight(), null);
        x1 = (p.getX() < x1 ? p.getX() : x1);
        y1 = (p.getY() < y1 ? p.getY() : y1);
        x2 = (p.getX() > x2 ? p.getX() : x2);
        y2 = (p.getY() > y2 ? p.getY() : y2);
        p = matrix.transform(rect.getX(), rect.getY() + rect.getHeight(), null);
        x1 = (p.getX() < x1 ? p.getX() : x1);
        y1 = (p.getY() < y1 ? p.getY() : y1);
        x2 = (p.getX() > x2 ? p.getX() : x2);
        y2 = (p.getY() > y2 ? p.getY() : y2);
        return new LBounds(x1, y1, x2 - x1, y2 - y1);
    }

    public LBounds getRectangleInverseTransformed(LMatrix matrix, ILBounds rect) throws LNoninvertibleMatrixException {
        return getRectangleInverseTransformed(matrix, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }

    public LBounds getRectangleInverseTransformed(LMatrix matrix, double x, double y, double width, double height) throws LNoninvertibleMatrixException {
        LPoint p = matrix.inverseTransform(x, y, null);
        double x1 = p.getX(), x2 = p.getX();
        double y1 = p.getY(), y2 = p.getY();
        p = matrix.inverseTransform(x + width, y, null);
        x1 = (p.getX() < x1 ? p.getX() : x1);
        y1 = (p.getY() < y1 ? p.getY() : y1);
        x2 = (p.getX() > x2 ? p.getX() : x2);
        y2 = (p.getY() > y2 ? p.getY() : y2);
        p = matrix.inverseTransform(x + width, y + height, null);
        x1 = (p.getX() < x1 ? p.getX() : x1);
        y1 = (p.getY() < y1 ? p.getY() : y1);
        x2 = (p.getX() > x2 ? p.getX() : x2);
        y2 = (p.getY() > y2 ? p.getY() : y2);
        p = matrix.inverseTransform(x, y + height, null);
        x1 = (p.getX() < x1 ? p.getX() : x1);
        y1 = (p.getY() < y1 ? p.getY() : y1);
        x2 = (p.getX() > x2 ? p.getX() : x2);
        y2 = (p.getY() > y2 ? p.getY() : y2);
        return new LBounds(x1, y1, x2 - x1, y2 - y1);
    }

    public LPoint getPointTransformed(LMatrix matrix, ILPoint point) {
        return getPointTransformed(matrix, point.getX(), point.getY());
    }

    public LPoint getPointTransformed(LMatrix matrix, double x, double y) {
        return matrix.transform(x, y, null);
    }

    public LPoint getPointInverseTransformed(LMatrix matrix, double x, double y) throws LNoninvertibleMatrixException {
        return matrix.inverseTransform(x, y, null);
    }

    public LObservable<LBounds> viewBounds() {
        return viewBounds;
    }

    public LBounds getViewBounds() {
        return viewBounds.get();
    }

    public void setViewBounds(LBounds viewBounds) {
        if (viewBounds == null) {
            throw new IllegalArgumentException("LCanvas.setViewBounds: viewBounds can't be null.");
        }
        viewBounds().set(viewBounds);
    }

    public ILCanvasFontRenderer getFontRenderer() {
        return fontRenderer;
    }

    public void setFontRenderer(ILCanvasFontRenderer fontRenderer) {
        this.fontRenderer = fontRenderer;
    }

    public LYosos<ILCanvasCommand> getCommands() {
        if (commands == null) {
            commands = new LObservable<>(new LYosos<>());
        }
        return commands.get();
    }

    @Xml
    public void add(ILCanvasCommand command) {
        addCommand(command);
    }

    @Xml
    public void addCommand(ILCanvasCommand command) {
        synchronized (getCommands()) {
            getCommands().add(command);
        }
    }

    public void removeCommand(ILCanvasCommand command) {
        synchronized (getCommands()) {
            getCommands().remove(command);
        }
    }

    public final LDouble rotation() {
        if (rotation == null) {
            rotation = new LDouble(0.0, 0.0, 360.0);            
            rotation.addListener(rotationListener);
        }
        return rotation;
    }

    public void setRotation(double degree) {
        rotation().set(degree);
    }

    public double getRotation() {
        return (rotation != null ? rotation.get() : 0.0);
    }

    public final LObservable<LColor> background() {
        if (background == null) {
            background = new LObservable<>();
        }
        return background;
    }

    public final void setBackground(LColor value) {
        background().set(value);
    }

    public final LColor getBackground() {
        return (background != null ? background.get() : null);
    }

    public final LBoolean scaleRotated() {
        if (scaleRotated == null) {
            scaleRotated = new LBoolean(true);
        }
        return scaleRotated;
    }

    public boolean isScaleRotated() {
        return (scaleRotated != null ? scaleRotated.get() : true);
    }

    public void setScaleRotated(boolean scaleRotated) {
        scaleRotated().set(scaleRotated);
    }

    /**
     * Indicates whether to preserve the aspect ratio of the canvas when scaling
     *
     * @return boolean property
     */
    public LBoolean preserveRatio() {
        if (preserveRatio == null) {
            preserveRatio = new LBoolean(true);
        }
        return preserveRatio;
    }

    public boolean isPreserveRatio() {
        return (preserveRatio != null ? preserveRatio.get() : true);
    }

    public void setPreserveRatio(boolean preserveRatio) {
        preserveRatio().set(preserveRatio);
    }

    public LInteger rotationSnap() {
        if (rotationSnap == null) {
            rotationSnap = new LInteger(0);
        }
        return rotationSnap;
    }

    public int getRotationSnap() {
        return (rotationSnap != null ? rotationSnap.get() : 0);
    }

    public void setRotationSnap(int rotationSnap) {
        rotationSnap().set(rotationSnap);
    }

    public LBoolean rotationEnabled() {
        if (rotationEnabled == null) {
            rotationEnabled = new LBoolean(false);
        }
        return rotationEnabled;
    }

    public boolean isRotationEnabled() {
        return (rotationEnabled != null ? rotationEnabled.get() : false);
    }

    public void setRotationEnabled(boolean rotationEnabled) {
        rotationEnabled().set(rotationEnabled);
    }

    public synchronized void clearCanvas() {
        getCommands().clear();
    }

    @SuppressWarnings("unchecked")
    public LObservable<LCanvasState> state() {
        if (state == null) {
            state = new LObservable<>(LCanvasState.PARSED);
            state.addListener(event -> this.notifyOnChanged(new LChangedEvent(this)));
        }
        return state;
    }

    public LCanvasState getState() {
        return (state != null ? state.get() : LCanvasState.PARSED);
    }

    public void setState(LCanvasState state) {
        state().set(state);
    }

    public boolean readFromString(Object parent, LMethod[] parentMethods, String xmlString) {
        clearCanvas();
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xmlString));
            Document xml = db.parse(is);
            this.parseXml(xml.getDocumentElement(), null);
        } catch (Exception e) {
            LLog.error(this, "Canvas.readFromString", e);
        }
        return true;
    }

    public boolean loadFromFile(Object parent, LMethod[] parentMethods, String filename) throws IOException, LParseException {
        clearCanvas();
        LXmlUtils.parseXmlFromFile(this, new File(filename));
        return true;
    }

    public boolean readFromInputStream(Object parent, LMethod[] parentMethods, InputStream stream) throws IOException, LParseException {
        clearCanvas();
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document xml = (Document) docBuilder.parse(stream);
            this.parseXml(xml.getDocumentElement(), null);
        } catch (SAXParseException err) {
            throw new IOException("Parsing error" + ", line "
                    + err.getLineNumber() + ", uri " + err.getSystemId()
                    + " " + err.getMessage());
        } catch (SAXException e) {
            Exception iex = e.getException();
            throw new IOException(((iex == null) ? e : iex).getMessage());
        } catch (ParserConfigurationException pce) {

        }
        return true;
    }

    @Override
    public void parseXml(Node n, LXmlParseInfo xmlParseInfo) throws LParseException {
        this.clearCanvas();
        LXmlUtils.parseXml(this, n, xmlParseInfo, null);
    }

    @Override
    public void toXml(Document doc, Element node) {
        throw new UnsupportedOperationException("Not supported yet.");
        /*LXmlUtils.setAttribute(node, "viewBounds", LXmlUtils.boundsToXmlStr(viewBounds));
        LXmlUtils.setAttribute(node, "contentBounds", LXmlUtils.boundsToXmlStr(contentBounds));
        //rotateAllowed
        LXmlUtils.setAttribute(node, "rotateAllowed", Boolean.toString(rotateAllowed));
        //rotation
        LXmlUtils.setAttribute(node, "rotation", LXmlUtils.integerPropertyToXmlStr(rotation));
        //rotationSnap
        LXmlUtils.setAttribute(node, "rotationSnap", LXmlUtils.integerToXmlStr(rotationSnap));
        //Shapes
        for (int i = 0; i < shapes.size(); i++) {
            LBounds s = shapes.get(i);
            if (s instanceof IMXmlSupport) {
                String childName = null;
                if (s instanceof LBounds) {
                    childName = "path";
                } else if (s instanceof LTextCounterShape) {
                    childName = "counter";
                } else if (s instanceof LTextShape) {
                    childName = "text";
                } else if (s instanceof LRectangle) {
                    childName = "rect";
                } else if (s instanceof LEllipse) {
                    childName = "ellipse";
                }
                if (childName != null) {
                    Element child = (Element) node.appendChild(doc.createElement(childName));
                    ((IMXmlSupport) s).saveXml(doc, child);
                    paints.get(i).saveXml(doc, child);
                }
            }
        }*/
    }

    public LYosos<LCanvas> getMarks() {
        if (marks == null) {
            marks = new LYosos<>();
        }
        return marks;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void execute(LCanvasRenderer canvasRenderer, long now) {
        super.execute(canvasRenderer, now);
        if (commands != null) {
            LYososIterator<ILCanvasCommand> it_cmd = new LYososIterator<>(getCommands());
            while ((!canvasRenderer.isRenderingCancelled()) && (it_cmd.hasNext())) {
                ILCanvasCommand cmd = it_cmd.next();
                if (cmd != null) {
                    cmd.execute(canvasRenderer, now);
                }
            }
        }
    }

    public record LAnimationDurations(int duration, boolean infinite){}
    
    public LAnimationDurations checkAnimations() {
        int duration = 0;
        boolean infinite = false;
        if (commands != null) {
            LYososIterator<ILCanvasCommand> it_cmd = new LYososIterator<>(getCommands());
            while (it_cmd.hasNext()) {
                ILCanvasCommand cmd = it_cmd.next();
                if ((cmd != null) && (cmd instanceof LShape)) {
                    LShape shape = (LShape) cmd;
                    if (shape.hasAnimations()) {
                        LYososIterator<ILAnimation> it_anim = new LYososIterator<>(shape.getAnimations());
                        while (it_anim.hasNext()) {
                            ILAnimation anim = it_anim.next();
                            duration = Math.max(duration, anim.getDelay() + anim.getDuration());
                            infinite = ((infinite) || (anim.isInfinite()));
                        }
                    }
                    if (shape instanceof LCanvas) {
                        LCanvas subCanvas = (LCanvas) shape;
                        LAnimationDurations subAnim = subCanvas.checkAnimations();
                        duration = Math.max(duration, subAnim.duration());
                        infinite = ((infinite) || (subAnim.infinite()));
                    }
                }
            }
        }
        return new LAnimationDurations(duration, infinite);
    }    
    
    public final LEventHandler<LChangedEvent> onChanged() {
        if (onChanged == null) {
            onChanged = new LEventHandler<>();
        }
        return onChanged;
    }

    public final void setOnChanged(ILHandler<LChangedEvent> value) {
        onChanged().set(value);
    }

    public final ILHandler<LChangedEvent> getOnChanged() {
        return (onChanged != null ? onChanged.get() : null);
    }

    private synchronized void notifyOnChanged(LChangedEvent changedEvent) {
        if (onChanged != null) {
            onChanged.fireEvent(changedEvent);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object clone() {
        try {
            LCanvas clone = (LCanvas) super.clone();
            clone.x = LDouble.clone(x);
            clone.y = LDouble.clone(y);
            clone.width = LDouble.clone(width);
            clone.height = LDouble.clone(height);
            clone.viewBounds = LObservable.clone(viewBounds);
            clone.commands = LObservable.clone(commands);
            clone.rotation = LDouble.clone(rotation);
            clone.rotationSnap = rotationSnap;
            clone.rotationEnabled = rotationEnabled;
            clone.scaleRotated = LBoolean.clone(scaleRotated);
            clone.background = LObservable.clone(background);
            return clone;
        } catch (Exception e) {
            throw new InternalError(e);
        }

    }

    @Override
    public String toString() {
        return LXmlUtils.classToString(this, Xml.class);
    }
    
    

}
