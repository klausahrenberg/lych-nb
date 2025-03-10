package com.ka.lych.graphics;

import com.ka.lych.exception.LException;
import com.ka.lych.geometry.ILBounds;
import com.ka.lych.geometry.LGeomUtils;
import com.ka.lych.observable.LString;
import com.ka.lych.xml.LXmlUtils;
import com.ka.lych.xml.LXmlUtils.LXmlParseInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author klausahrenberg
 */
public class LRectangle extends LShape {

    double[] radiuses;

    public LRectangle() {
        this(0, 0, 0, 0, 0, 0);
        neededShapeAttributes = new String[]{"rx", "ry"};
    }

    public LRectangle(Node n, LXmlParseInfo xmlParseInfo) throws LException {
        this(0, 0, 0, 0, 0, 0);
        parseXml(n, xmlParseInfo);
        neededShapeAttributes = new String[]{"rx", "ry"};
    }

    public LRectangle(ILBounds bounds) {
        this(bounds.x().get(), bounds.y().get(), bounds.width().get(), bounds.height().get());
    }    
    
    public LRectangle(double x, double y, double width, double height) {
        this(x, y, width, height, 0, 0);
    }

    public LRectangle(double x, double y, double width, double height, double rx, double ry) {
        //super(5, 8);
        //setBounds(_x, _y, width, height);   
        super(9, 24);
        setRadiuses(rx, ry);        
        bounds(x, y, width, height);
    }

    protected final void setRadiuses(double rx, double ry) {
        if ((rx > 0) || (ry > 0)) {
            if (radiuses == null) {
                radiuses = new double[8];
                for (int i = 0; i < 4; i++) {
                    radiuses[i * 2] = rx;
                    radiuses[i * 2 + 1] = ry;
                }
            }
        } else {
            radiuses = null;
        }
    }

    @Override
    protected void createPath() {
        countPoints = numCoords = 0;
        
        if ((LGeomUtils.isNotEqual(width().get(), 0.0)) && (LGeomUtils.isNotEqual(height().get(), 0.0))) {
            double[] xpoints = new double[4];
            double[] ypoints = new double[4];
            xpoints[0] = x().get();
            ypoints[0] = y().get();
            xpoints[1] = x().get() + width().get();
            ypoints[1] = y().get();
            xpoints[2] = x().get() + width().get();
            ypoints[2] = y().get() + height().get();
            xpoints[3] = x().get();
            ypoints[3] = y().get() + height().get();
            this.createPath(xpoints, ypoints, radiuses, true);
        }
    }

    @Override
    public void parseXml(Node n, LXmlParseInfo xmlParseInfo) throws LException {        
        this.parseXmlCommonAttributes(n, xmlParseInfo);
        if (n.hasAttributes()) {
            setRadiuses(LXmlUtils.xmlAttributeToDouble(n, "rx", 0.0), 
                        LXmlUtils.xmlAttributeToDouble(n, "ry", 0.0));                
        } else if (!LString.isEmpty(n.getTextContent())) {            
            LXmlUtils.xmlStrToBounds(n.getTextContent(), this);
        } else {
            bounds(0, 0, 0, 0);
        }
    }

    @Override
    public void toXml(Document doc, Element node) {
        throw new UnsupportedOperationException("Not suported yet.");
    }

}
