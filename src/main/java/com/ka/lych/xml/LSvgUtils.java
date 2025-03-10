package com.ka.lych.xml;

import com.ka.lych.exception.LException;
import com.ka.lych.geometry.LBounds;
import com.ka.lych.graphics.ILCanvasCommand;
import com.ka.lych.graphics.LCanvas;
import com.ka.lych.graphics.LColor;
import com.ka.lych.graphics.LGraphicStatePop;
import com.ka.lych.graphics.LGraphicStatePush;
import com.ka.lych.graphics.LMatrix;
import com.ka.lych.graphics.LPaint;
import com.ka.lych.graphics.LShape;
import com.ka.lych.graphics.LStroke;
import com.ka.lych.graphics.LStrokeLineCap;
import com.ka.lych.graphics.LStrokeLineJoin;
import com.ka.lych.graphics.LStyle;
import com.ka.lych.list.LList;
import com.ka.lych.util.ILParseable;
import com.ka.lych.util.LLog;
import com.ka.lych.xml.LXmlUtils.LXmlParseInfo;
import java.util.Optional;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 *
 * @author klausahrenberg
 */
public abstract class LSvgUtils {

    static final String SVG = "svg";
    static final String SVG_FILL = "fill";
    static final String SVG_GROUP = "g";
    static final String SVG_ID = "id";
    static final String SVG_PATH = "path";
    static final String SVG_STROKE = "stroke";
    static final String SVG_STROKE_LINECAP = "stroke-linecap";
    static final String SVG_STROKE_LINEJOIN = "stroke-linejoin";
    static final String SVG_STROKE_WIDTH = "stroke-width";
    static final String SVG_TRANSFORM = "transform";
    static final String SVG_TRANSLATE = "translate";
    static final String SVG_VIEW_BOX = "viewBox";
    static final String SVG_XMLNS = "xmlns";
    
    protected static LList<String> FILLS_AND_STROKES = new LList<>(new String[]{SVG_FILL, SVG_STROKE, SVG_STROKE_LINECAP, SVG_STROKE_LINEJOIN, SVG_STROKE_WIDTH});    

    public static void parseXml(LCanvas canvas, Node xmlNode, LXmlParseInfo xmlParseInfo) throws LException {
        _parseNode(canvas, xmlNode, xmlParseInfo);
    }

    static void _parseNode(LCanvas canvas, Node node, LXmlParseInfo xmlParseInfo) throws LException {
        if (LXmlUtils.isNotExcluded(node.getNodeName(), FILLS_AND_STROKES)) {
            //LLog.test(LSvgUtils.class, "parse svg node '%s'", node.getNodeName());
            ILCanvasCommand cmd = null;
            switch (node.getNodeName()) {
                case SVG_VIEW_BOX -> {
                    canvas.viewBounds(ILParseable.of(LBounds.class, node.getTextContent()));
                    canvas.viewBounds().ifPresent(b -> canvas.width(b.width().get()).height(b.height().get()));
                }    
                case SVG_TRANSFORM -> _parseTransforms(canvas, node, xmlParseInfo);
                case SVG_GROUP, SVG -> {
                    canvas.add(LGraphicStatePush.getInstance());
                    _parseFillAndStroke(canvas, node, xmlParseInfo);
                    _parseStrokeAttributes(canvas, node, xmlParseInfo).ifPresent(stroke -> canvas.add(stroke));
                    //1. Attributes
                    if (node.hasAttributes()) {
                        NamedNodeMap attrList = node.getAttributes();
                        for (int i = 0; i < attrList.getLength(); i++) {
                            _parseNode(canvas, attrList.item(i), xmlParseInfo);
                        }
                    }
                    //2. Nodes
                    for (int i = 0; i < node.getChildNodes().getLength(); i++) {
                        _parseNode(canvas, node.getChildNodes().item(i), xmlParseInfo);
                    }
                    canvas.add(LGraphicStatePop.getInstance());
                }
                case SVG_PATH -> {
                    var s = new LShape();
                    s.parseXml(node, xmlParseInfo);
                    canvas.add(s);
                }
                case SVG_XMLNS, SVG_ID -> {}
                default -> {
                    LLog.error("Unknown SVG command: '" + node.getNodeName() + "' - ignored: '" + node.getTextContent() + "'");
                }
            }
        }
    }
    
    static void _parseTransforms(LCanvas canvas, Node node, LXmlParseInfo xmlParseInfo) throws LException {
        var sb = node.getTextContent().toLowerCase().trim();
        while (sb.length() > 0) {
            sb = _parseTransform(canvas, sb);
        }
    }

    static String _parseTransform(LCanvas canvas, String sb) throws LException {
        if (sb.startsWith(SVG_TRANSLATE)) {
            sb = sb.substring(SVG_TRANSLATE.length());
            var a = sb.indexOf("(");
            var b = sb.indexOf(")");
            if ((a >= 0) && (a < b)) {
                var s = sb.substring(a + 1, b);
                //LLog.test(LSvgUtils.class, "coords are '%s'", s);
                double[] coord = LXmlUtils.xmlStrToDoubleArray(new StringBuilder(s), 2);
                canvas.add(LMatrix.getTranslateInstance(coord[0], coord[1]));
            } else {
                throw new LException("Can't find brackets for transformation: '%s'", sb);
            }
            return sb.substring(b + 1).trim();
        } else { 
            throw new LException("Unknown command for transformation: '%s'", sb);
        }
    }
    
    static void _parseFillAndStroke(LCanvas canvas, Node node, LXmlParseInfo xmlParseInfo) throws LException {
        if (node.hasAttributes()) {
            var sw = node.getAttributes().getNamedItem(SVG_FILL);
            if (sw != null) {
                var fill = new LPaint();
                fill.style().add(LStyle.FILL);
                fill.color(LColor.of(sw.getTextContent()));
                canvas.add(fill);
            }
            sw = node.getAttributes().getNamedItem(SVG_STROKE);
            if (sw != null) {
                var stroke = new LPaint();
                stroke.style().add(LStyle.STROKE);
                stroke.color(LColor.of(sw.getTextContent()));
                canvas.add(stroke);
            }
        }
    }
    
    static Optional<LStroke> _parseStrokeAttributes(LCanvas canvas, Node node, LXmlParseInfo xmlParseInfo) throws LException {
        var stroke = new LStroke();
        var found = false;
        if (node.hasAttributes()) {
            var sw = node.getAttributes().getNamedItem(SVG_STROKE_WIDTH);
            if (sw != null) {
                stroke.setWidth(LXmlUtils.xmlStrToDouble(sw.getTextContent()));
                found = true;
            }
            sw = node.getAttributes().getNamedItem(SVG_STROKE_LINECAP);
            if (sw != null) {
                stroke.setCap(LXmlUtils.xmlStrToEnum(sw.getTextContent(), LStrokeLineCap.class));
                found = true;
            }
            sw = node.getAttributes().getNamedItem(SVG_STROKE_LINEJOIN);
            if (sw != null) {
                stroke.setJoin(LXmlUtils.xmlStrToEnum(sw.getTextContent(), LStrokeLineJoin.class));
                found = true;
            }
        }
        return (found ? Optional.of(stroke) : Optional.empty());
    }

}
