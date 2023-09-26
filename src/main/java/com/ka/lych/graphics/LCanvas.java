package com.ka.lych.graphics;

import com.ka.lych.annotation.Json;
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
import com.ka.lych.exception.LParseException;
import com.ka.lych.geometry.ILPoint;
import com.ka.lych.geometry.LScaleMode;
import com.ka.lych.graphics.anim.ILAnimation;
import com.ka.lych.list.LIterator;
import com.ka.lych.list.LList;
import com.ka.lych.observable.*;
import com.ka.lych.util.*;
import com.ka.lych.util.LReflections.LMethod;
import com.ka.lych.xml.LSvgUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import com.ka.lych.xml.LXmlUtils.LXmlParseInfo;

/**
 *
 * @author klausahrenberg
 */
public class LCanvas extends LShape<LCanvas>
        implements ILXmlSupport, ILCloneable {

    @Json
    protected LObject<LBounds> _viewBounds;
    @Json
    protected LDouble rotation;
    @Json
    protected LInteger rotationSnap;
    @Json
    protected LBoolean rotationEnabled;
    @Json
    protected LBoolean scaleRotated;
    @Json
    protected LBoolean preserveRatio;
    @Json
    protected LObject<LColor> background;
    @Json
    private LList<LCanvas> marks;
    @Json
    protected LObject<LCanvasState> state;
    @Json
    protected LObject<LList<ILCanvasCommand>> commands;

    protected ILCanvasFontRenderer fontRenderer;
    
    private LEventHandler<LChangedEvent> onChanged;

    public LCanvas() {
        this(500, 300);
    }

    public void initialize(URL location, ResourceBundle resources) {
    }

    public LCanvas(double viewBoundsWidth, double viewBoundsHeight) {
        _viewBounds = new LObject<>(new LBounds(0, 0, viewBoundsWidth, viewBoundsHeight));
    }

    private final ILChangeListener<Double, LDouble> rotationListener = change -> {
        if (marks != null) {
            for (LCanvas mark : marks) {
                mark.setRotation(mark.getRotation() + this.getRotation() - change.oldValue().doubleValue());
            }
        }
    };

    /**
     *
     * @param scaleMode
     * @param scaleFactor
     * @param paintBounds - bounds where the canvas will painted inside
     * @param translatePaintBoundsLocation - if true, the initialMatrix will be
 translated by _x, _y location of paintBounds
     * @param defaultMatrix
     * @return
     */
    public LMatrix getInitialMatrix(LScaleMode scaleMode, double scaleFactor, LBounds paintBounds, boolean translatePaintBoundsLocation, LMatrix defaultMatrix) {
        LMatrix m = (defaultMatrix == null ? new LMatrix() : defaultMatrix);
        //Create a test matrix to get the _x,_y offset coordinates
        LMatrix cm = (LMatrix) m.clone();
        if ((paintBounds != null) || (isScaleRotated())) {
            cm.rotate(Math.toRadians(getRotation()));
        }
        if (!LGeomUtils.isEqual(scaleFactor, 1.0, LGeomUtils.DEFAULT_DOUBLE_PRECISION)) {
            cm.scale(scaleFactor, scaleFactor);
        }
        LBounds s = getViewBoundsTransformed(cm);
        //1. Correct the _x,_y position depending on the result of scaled and rotated matrix
        double translateX = -s.x().get();
        double translateY = -s.y().get();
        if (paintBounds != null) {
            //2. If paintbounds there, correct the _x,_y position
            if (translatePaintBoundsLocation) {
                translateX += paintBounds.x().get();
                translateY += paintBounds.y().get();
            }
            //3. if the paintbounds don't fit to the resulting space from test matrix, center the canvas inside the paintbounds
            switch (scaleMode) {
                case FIT_PAGE:
                    if (!LGeomUtils.isEqual(paintBounds.height().get(), s.height().get(), LGeomUtils.DEFAULT_DOUBLE_PRECISION)) {
                        translateY += (paintBounds.height().get() / 2 - s.height().get() / 2);
                    }
                // no break
                case FIT_WIDTH:
                    if (!LGeomUtils.isEqual(paintBounds.width().get(), s.width().get(), LGeomUtils.DEFAULT_DOUBLE_PRECISION)) {
                        translateX += (paintBounds.width().get() / 2 - s.width().get() / 2);
                    }
                    break;
                case FACTOR:
            }
        }
        //m.translate(-s._x() - translateX, -s._y() - translateY);
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
        return getRectangleTransformed(matrix, viewBounds().get());
    }

    public LBounds getShapeTransformed(LMatrix matrix, LShape shape) {
        if (!shape.isRotatable()) {
            matrix.rotate(Math.toRadians(-this.getRotation()), shape.x().get() + shape.width().get() / 2, shape.y().get() + shape.height().get() / 2);
        }
        LBounds b = getRectangleTransformed(matrix, shape);
        if (!shape.isRotatable()) {
            matrix.rotate(Math.toRadians(this.getRotation()), shape.x().get() + shape.width().get() / 2, shape.y().get() + shape.height().get() / 2);
        }
        return b;
    }

    private LBounds getRectangleTransformed(LMatrix matrix, ILBounds rect) {
        LPoint p = matrix.transform(rect.x().get(), rect.y().get(), null);
        double x1 = p.x().get(), x2 = p.x().get();
        double y1 = p.y().get(), y2 = p.y().get();
        p = matrix.transform(rect.x().get() + rect.width().get(), rect.y().get(), null);
        x1 = (p.x().get() < x1 ? p.x().get() : x1);
        y1 = (p.y().get() < y1 ? p.y().get() : y1);
        x2 = (p.x().get() > x2 ? p.x().get() : x2);
        y2 = (p.y().get() > y2 ? p.y().get() : y2);
        p = matrix.transform(rect.x().get() + rect.width().get(), rect.y().get() + rect.height().get(), null);
        x1 = (p.x().get() < x1 ? p.x().get() : x1);
        y1 = (p.y().get() < y1 ? p.y().get() : y1);
        x2 = (p.x().get() > x2 ? p.x().get() : x2);
        y2 = (p.y().get() > y2 ? p.y().get() : y2);
        p = matrix.transform(rect.x().get(), rect.y().get() + rect.height().get(), null);
        x1 = (p.x().get() < x1 ? p.x().get() : x1);
        y1 = (p.y().get() < y1 ? p.y().get() : y1);
        x2 = (p.x().get() > x2 ? p.x().get() : x2);
        y2 = (p.y().get() > y2 ? p.y().get() : y2);
        return new LBounds(x1, y1, x2 - x1, y2 - y1);
    }

    public LBounds getRectangleInverseTransformed(LMatrix matrix, ILBounds rect) throws LNoninvertibleMatrixException {
        return getRectangleInverseTransformed(matrix, rect.x().get(), rect.y().get(), rect.width().get(), rect.height().get());
    }

    public LBounds getRectangleInverseTransformed(LMatrix matrix, double x, double y, double width, double height) throws LNoninvertibleMatrixException {
        LPoint p = matrix.inverseTransform(x, y, null);
        double x1 = p.x().get(), x2 = p.x().get();
        double y1 = p.y().get(), y2 = p.y().get();
        p = matrix.inverseTransform(x + width, y, null);
        x1 = (p.x().get() < x1 ? p.x().get() : x1);
        y1 = (p.y().get() < y1 ? p.y().get() : y1);
        x2 = (p.x().get() > x2 ? p.x().get() : x2);
        y2 = (p.y().get() > y2 ? p.y().get() : y2);
        p = matrix.inverseTransform(x + width, y + height, null);
        x1 = (p.x().get() < x1 ? p.x().get() : x1);
        y1 = (p.y().get() < y1 ? p.y().get() : y1);
        x2 = (p.x().get() > x2 ? p.x().get() : x2);
        y2 = (p.y().get() > y2 ? p.y().get() : y2);
        p = matrix.inverseTransform(x, y + height, null);
        x1 = (p.x().get() < x1 ? p.x().get() : x1);
        y1 = (p.y().get() < y1 ? p.y().get() : y1);
        x2 = (p.x().get() > x2 ? p.x().get() : x2);
        y2 = (p.y().get() > y2 ? p.y().get() : y2);
        return new LBounds(x1, y1, x2 - x1, y2 - y1);
    }

    public LPoint getPointTransformed(LMatrix matrix, ILPoint point) {
        return getPointTransformed(matrix, point.x().get(), point.y().get());
    }

    public LPoint getPointTransformed(LMatrix matrix, double x, double y) {
        return matrix.transform(x, y, null);
    }

    public LPoint getPointInverseTransformed(LMatrix matrix, double x, double y) throws LNoninvertibleMatrixException {
        return matrix.inverseTransform(x, y, null);
    }

    public LObject<LBounds> viewBounds() {
        return _viewBounds;
    }

    public void viewBounds(LBounds viewBounds) {
        LObjects.requireNonNull(viewBounds, "LCanvas.setViewBounds: viewBounds can't be null.");
        viewBounds().set(viewBounds);
    }

    public ILCanvasFontRenderer getFontRenderer() {
        return fontRenderer;
    }

    public void setFontRenderer(ILCanvasFontRenderer fontRenderer) {
        this.fontRenderer = fontRenderer;
    }

    public LList<ILCanvasCommand> getCommands() {
        if (commands == null) {
            commands = new LObject<>(new LList<>());
        }
        return commands.get();
    }

    @Json
    public void add(ILCanvasCommand command) {
        addCommand(command);
    }

    @Json
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

    public final LObject<LColor> background() {
        if (background == null) {
            background = new LObject<>();
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
    public LObject<LCanvasState> state() {
        if (state == null) {
            state = new LObject<>(LCanvasState.PARSED);
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
            LLog.error("Canvas.readFromString", e);
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
        if (LString.equalsIgnoreCase(n.getNodeName(), "svg")) {
            LSvgUtils.parseXml(this, n, xmlParseInfo);
        } else {
            LXmlUtils.parseXml(this, n, xmlParseInfo, null);
        }
    }

    @Override
    public void toXml(Document doc, Element node) {
        throw new UnsupportedOperationException("Not supported yet.");
        /*LXmlUtils.setAttribute(node, "_viewBounds", LXmlUtils.boundsToXmlStr(_viewBounds.get()));
        //LXmlUtils.setAttribute(node, "contentBounds", LXmlUtils.boundsToXmlStr(contentBounds));
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

    public LList<LCanvas> getMarks() {
        if (marks == null) {
            marks = new LList<>();
        }
        return marks;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void execute(LCanvasRenderer canvasRenderer, long now) {
        super.execute(canvasRenderer, now);
        if (commands != null) {
            LIterator<ILCanvasCommand> it_cmd = new LIterator<>(getCommands());
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
            LIterator<ILCanvasCommand> it_cmd = new LIterator<>(getCommands());
            while (it_cmd.hasNext()) {
                ILCanvasCommand cmd = it_cmd.next();
                if ((cmd != null) && (cmd instanceof LShape)) {
                    LShape shape = (LShape) cmd;
                    if (shape.hasAnimations()) {
                        LIterator<ILAnimation> it_anim = new LIterator<>(shape.getAnimations());
                        while (it_anim.hasNext()) {
                            ILAnimation anim = it_anim.next();
                            duration = Math.max(duration, anim.delay() + anim.duration());
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
    public LCanvas clone() {
        try {
            LCanvas clone = (LCanvas) super.clone();
            clone.x(_x.get());
            clone.y(_y.get());
            clone.width(_width.get());
            clone.height(_height.get());
            clone.viewBounds().set(_viewBounds.get());
            clone.commands = commands.clone();
            clone.rotation = rotation.clone();
            clone.rotationSnap = rotationSnap;
            clone.rotationEnabled = rotationEnabled;
            clone.scaleRotated = scaleRotated.clone();
            clone.background = background.clone();
            return clone;
        } catch (Exception e) {
            throw new InternalError(e);
        }

    }

    @Override
    public String toString() {
        return LXmlUtils.classToString(this);
    }
    
    

}
