package com.ka.lych.graphics;

import com.ka.lych.exception.LException;
import com.ka.lych.xml.LXmlUtils;
import com.ka.lych.xml.LXmlUtils.LXmlParseInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author klausahrenberg
 */
public class LEllipse extends LShape {

    public LEllipse() {
        super(6, 26);
        neededShapeAttributes = new String[]{"cx", "cy", "rx", "ry"};
    }

    public LEllipse(Node n, LXmlParseInfo xmlParseInfo) throws LException {
        this();
        parseXml(n, null);
        neededShapeAttributes = new String[]{"cx", "cy", "rx", "ry"};
    }    
    private static final double pCV = 0.5 + LShape.KAPPA * 0.5;
    private static final double nCV = 0.5 - LShape.KAPPA * 0.5;
    private static double ctrlpts[][] = {
        {1.0, pCV, pCV, 1.0, 0.5, 1.0},
        {nCV, 1.0, 0.0, pCV, 0.0, 0.5},
        {0.0, nCV, nCV, 0.0, 0.5, 0.0},
        {pCV, 0.0, 1.0, nCV, 1.0, 0.5}
    };

    @Override
    protected void createPath() {
        
        countPoints = numCoords = 0;
        if ((width().get() > 0) && (height().get() > 0)) {
            //1. LineTo start point
            double[] ctrls = ctrlpts[3];
            pointTypes[countPoints++] = SEG_MOVETO;
            doubleCoords[numCoords++] = x().get() + ctrls[4] * width().get();
            doubleCoords[numCoords++] = y().get() + ctrls[5] * height().get();
            //2..4. Curve
            for (int index = 1; index <= 4; index++) {
                ctrls = ctrlpts[index - 1];
                pointTypes[countPoints++] = SEG_CUBICTO;
                doubleCoords[numCoords++] = x().get() + ctrls[0] * width().get();
                doubleCoords[numCoords++] = y().get() + ctrls[1] * height().get();
                doubleCoords[numCoords++] = x().get() + ctrls[2] * width().get();
                doubleCoords[numCoords++] = y().get() + ctrls[3] * height().get();
                doubleCoords[numCoords++] = x().get() + ctrls[4] * width().get();
                doubleCoords[numCoords++] = y().get() + ctrls[5] * height().get();
            }
            //5. closePath
            pointTypes[countPoints++] = SEG_CLOSE;
        }
    }

    @Override
    public void parseXml(Node n, LXmlParseInfo xmlParseInfo) throws LException {
        this.parseXmlCommonAttributes(n, xmlParseInfo);
        //<ellipse cx="200" cy="80" rx="100" ry="50"        
        if (LXmlUtils.countExistingAttributes(n, neededShapeAttributes) == 4) {
            double cx = LXmlUtils.xmlStrToDouble(n.getAttributes().getNamedItem("cx").getTextContent());
            double cy = LXmlUtils.xmlStrToDouble(n.getAttributes().getNamedItem("cy").getTextContent());
            double rx = LXmlUtils.xmlStrToDouble(n.getAttributes().getNamedItem("rx").getTextContent());
            double ry = LXmlUtils.xmlStrToDouble(n.getAttributes().getNamedItem("ry").getTextContent());
            bounds(cx - rx, cy - ry, 2 * rx, 2 * ry);
        } else {
            bounds(0, 0, 0, 0);
        }
    }

    @Override
    public void toXml(Document doc, Element node) {
        throw new UnsupportedOperationException("Not suported yet.");
    }

}
