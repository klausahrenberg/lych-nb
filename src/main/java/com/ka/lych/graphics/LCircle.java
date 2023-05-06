package com.ka.lych.graphics;

import com.ka.lych.util.LParseException;
import com.ka.lych.xml.LXmlUtils;
import com.ka.lych.xml.LXmlUtils.LXmlParseInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author klausahrenberg
 */
public class LCircle extends LEllipse {

    public LCircle() {
        super();
        neededShapeAttributes = new String[]{"cx", "cy", "r"};
    }

    public LCircle(Node n, LXmlParseInfo xmlParseInfo) throws LParseException {
        super(n, xmlParseInfo);        
        neededShapeAttributes = new String[]{"cx", "cy", "r"};
    }

    @Override
    public void parseXml(Node n, LXmlParseInfo xmlParseInfo) throws LParseException {
        this.parseXmlCommonAttributes(n, xmlParseInfo);
        //<circle cx="50" cy="50" r="40"
        if (LXmlUtils.countExistingAttributes(n, neededShapeAttributes) == 3) {
            double cx = LXmlUtils.xmlStrToDouble(n.getAttributes().getNamedItem("cx").getTextContent());
            double cy = LXmlUtils.xmlStrToDouble(n.getAttributes().getNamedItem("cy").getTextContent());
            double r = LXmlUtils.xmlStrToDouble(n.getAttributes().getNamedItem("r").getTextContent());
            setBounds(cx - r, cy - r, 2 * r, 2 * r);
        } else {
            setBounds(0, 0, 0, 0);
        }
    }

    @Override
    public void toXml(Document doc, Element node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
