package com.ka.lych.graphics;

import com.ka.lych.annotation.Json;
import java.util.EnumSet;
import com.ka.lych.geometry.LAlignment;
import com.ka.lych.observable.*;
import com.ka.lych.util.ILConstants;
import com.ka.lych.util.LParseException;
import com.ka.lych.xml.LXmlUtils.LXmlParseInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author klausahrenberg
 */
public class LTextShape extends LShape
        implements ILConstants {

    @Json
    protected LString text;
    @Json
    protected LObservable<EnumSet<LTextStyle>> textStyle;
    @Json
    protected LString fontFamily;
    @Json
    protected LObservable<LAlignment> alignment;
    @Json
    protected LBoolean adjustFontHeight;
    @Json
    protected LBoolean adjustSizeForWidth;
    @Json
    protected LBoolean textEditable;
    @Json
    protected LBoolean paintText;
    @Json
    protected LBoolean multiLine;
    @Json
    protected LBoolean wrapText;
    @Json
    protected LDouble textSize;

    public LTextShape() {
    }

    public LTextShape(String text) {
        this();
    }

    public LTextShape(Node n, LXmlParseInfo xmlParseInfo) throws LParseException {
        this();
        parseXml(n, xmlParseInfo);
    }

    public LString text() {
        if (text == null) {
            text = new LString();
        }
        return text;
    }

    public String getText() {
        return (text != null ? text.get() : null);
    }

    public void setText(String text) {
        text().set(text);
    }

    public LObservable<EnumSet<LTextStyle>> textStyle() {
        if (textStyle == null) {
            textStyle = new LObservable<>(EnumSet.of(LTextStyle.NORMAL));
        }
        return textStyle;
    }

    public EnumSet<LTextStyle> getTextStyle() {
        return textStyle().get();
    }

    public void setTextStyle(EnumSet<LTextStyle> textStyle) {
        textStyle().set(textStyle);
    }

    public LObservable<LAlignment> alignment() {
        if (alignment == null) {
            alignment = new LObservable<>(LAlignment.CENTER_LEFT);
        }
        return alignment;
    }

    public LAlignment getAlignment() {
        return (alignment != null ? alignment.get() : LAlignment.CENTER_LEFT);
    }

    public void setAlignment(LAlignment alignment) {
        alignment().set(alignment);
    }

    public LBoolean adjustFontHeight() {
        if (adjustFontHeight == null) {
            adjustFontHeight = new LBoolean(false);
        }
        return adjustFontHeight;
    }

    public boolean isAdjustFontHeight() {
        return (adjustFontHeight != null ? adjustFontHeight.get() : false);
    }

    public void setAdjustFontHeight(boolean adjustFontHeight) {
        adjustFontHeight().set(adjustFontHeight);
    }

    public LString fontFamily() {
        if (fontFamily == null) {
            fontFamily = new LString("Arial");
        }
        return fontFamily;
    }

    public String getFontFamily() {
        return (fontFamily != null ? fontFamily.get() : "Arial");
    }

    public void setFontFamily(String fontFamily) {
        fontFamily().set(fontFamily);
    }

    public LBoolean paintText() {
        if (paintText == null) {
            paintText = new LBoolean(true);
        }
        return paintText;
    }

    public boolean isPaintText() {
        return (paintText != null ? paintText.get() : true);
    }

    public void setPaintText(boolean paintText) {
        paintText().set(paintText);
    }

    public LBoolean adjustSizeForWidth() {
        if (adjustSizeForWidth == null) {
            adjustSizeForWidth = new LBoolean(false);
        }
        return adjustSizeForWidth;
    }

    public boolean isAdjustSizeForWidth() {
        return (adjustSizeForWidth != null ? adjustSizeForWidth.get() : false);
    }

    public void setAdjustSizeForWidth(boolean adjustSizeForWidth) {
        adjustSizeForWidth().set(adjustSizeForWidth);
    }

    public LBoolean textEditable() {
        if (textEditable == null) {
            textEditable = new LBoolean(false);
        }
        return textEditable;
    }

    public boolean isTextEditable() {
        return (textEditable != null ? textEditable.get() : false);
    }

    public void setTextEditable(boolean textEditable) {
        textEditable().set(textEditable);
    }

    public LBoolean multiLine() {
        if (multiLine == null) {
            multiLine = new LBoolean(false);
        }
        return multiLine;
    }

    public boolean isMultiLine() {
        return (multiLine != null ? multiLine.get() : false);
    }

    public void setMultiLine(boolean multiLine) {
        multiLine().set(multiLine);
    }

    public LBoolean wrapText() {
        if (wrapText == null) {
            wrapText = new LBoolean(false);
        }
        return wrapText;
    }

    public boolean isWrapText() {
        return (wrapText != null ? wrapText.get() : false);
    }

    public void setWrapText(boolean wrapText) {
        wrapText().set(wrapText);
    }

    public LDouble textSize() {
        if (textSize == null) {
            textSize = new LDouble(12.0);
        }
        return textSize;
    }

    public double getTextSize() {
        return (textSize != null ? textSize.get() : 12);
    }

    public void setTextSize(double textSize) {
        if (textSize <= 0) {
            throw new IllegalArgumentException("nativeFontSize must be >0");
        }
        textSize().set(textSize);
    }

    @Override
    public void parseXml(Node n, LXmlParseInfo xmlParseInfo) throws LParseException {
        super.parseXml(n, xmlParseInfo);
    }

    @Override
    public void toXml(Document doc, Element node) {
        super.toXml(doc, node);
    }

    @Override
    public void execute(LCanvasRenderer canvasRenderer, long timeLine) {
        canvasRenderer.drawTextShape(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object clone() {
        try {
            LTextShape ts = (LTextShape) super.clone();
            ts.text = LString.clone(text);
            ts.textStyle = LObservable.clone(textStyle);
            ts.fontFamily = LString.clone(fontFamily);
            ts.alignment = LObservable.clone(alignment);
            ts.adjustFontHeight = LBoolean.clone(adjustFontHeight);
            ts.adjustSizeForWidth = LBoolean.clone(adjustSizeForWidth);
            ts.textEditable = LBoolean.clone(textEditable);
            //ts.paintText - not copied
            ts.multiLine = LBoolean.clone(multiLine);
            ts.wrapText = LBoolean.clone(wrapText);
            ts.textSize = LDouble.clone(textSize);
            return ts;
        } catch (Exception e) {
            throw new InternalError(e);
        }
    }

}
