package com.ka.lych.graphics;

import com.ka.lych.observable.LString;
import com.ka.lych.util.ILConstants;
import com.ka.lych.util.ILParseable;
import com.ka.lych.util.LLog;
import com.ka.lych.util.LParseException;
import com.ka.lych.xml.*;
import com.ka.lych.xml.LXmlUtils.LXmlParseInfo;
import java.util.function.Function;
import org.w3c.dom.*;

/**
 *
 * @author klausahrenberg
 */
public class LColor
        implements ILXmlSupport, Comparable<LColor>, ILParseable {

    public static Function<String, Integer> SYSTEM_COLOR_PROVIDER;

    public final static LColor WHITE = new LColor(255, 255, 255);
    public final static LColor LIGHT_GRAY = new LColor(192, 192, 192);
    public final static LColor GRAY = new LColor(128, 128, 128);
    public final static LColor DARK_GRAY = new LColor(64, 64, 64);
    public final static LColor BLACK = new LColor(0, 0, 0);
    public final static LColor RED = new LColor(255, 0, 0);
    public final static LColor PINK = new LColor(255, 175, 175);
    public final static LColor ORANGE = new LColor(255, 200, 0);
    public final static LColor YELLOW = new LColor(255, 255, 0);
    public final static LColor GREEN = new LColor(0, 255, 0);
    public final static LColor MAGENTA = new LColor(255, 0, 255);
    public final static LColor CYAN = new LColor(0, 255, 255);
    public final static LColor BLUE = new LColor(0, 0, 255);
    public final static LColor TRANSPARENT = new LColor(0, 0, 0, 0);

    protected int value;

    public LColor() {
        this(0, 0, 0, 0);
    }

    public LColor(int rgbValue) {
        this.value = rgbValue;
    }

    public LColor(int r, int g, int b) {
        this(r, g, b, 255);
    }

    public LColor(int r, int g, int b, int a) {
        setValue(r, g, b, a);
    }

    public LColor(Node n, LXmlParseInfo xmlParseInfo) throws LParseException {
        parseXml(n, xmlParseInfo);
    }

    private void setValue(int r, int g, int b, int a) {
        value = rgbValue(r, g, b, a);
    }

    private static void testColorValueRange(int r, int g, int b, int a) {
        boolean rangeError = false;
        String badComponentString = "";

        if (a < 0 || a > 255) {
            rangeError = true;
            badComponentString = badComponentString + " Alpha " + a;
        }
        if (r < 0 || r > 255) {
            rangeError = true;
            badComponentString = badComponentString + " Red " + r;
        }
        if (g < 0 || g > 255) {
            rangeError = true;
            badComponentString = badComponentString + " Green " + g;
        }
        if (b < 0 || b > 255) {
            rangeError = true;
            badComponentString = badComponentString + " Blue " + b;
        }
        if (rangeError == true) {
            throw new IllegalArgumentException("Color parameter outside of expected range:"
                    + badComponentString);
        }
    }

    public static int clip(double value) {
        int result = (int) Math.round(value);
        result = (result < 0 ? 0 : (result > 255 ? 255 : result));
        return result;
    }

    public int getRGB() {
        return value;
    }

    public int getRed() {
        return (getRGB() >> 0) & 0xFF;
    }

    public int getGreen() {
        return (getRGB() >> 8) & 0xFF;
    }

    public int getBlue() {
        return (getRGB() >> 16) & 0xFF;
    }

    public int getAlpha() {
        return (getRGB() >> 24) & 0xff;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[r=" + getRed() + ", g=" + getGreen() + ", b=" + getBlue() + ", a=" + getAlpha() + "]";
    }

    public String toXmlHexStr() {
        return toXmlHexStr(this);
    }

    public static String toXmlHexStr(LColor c) {
        return (c != null
                ? ILConstants.KEYWORD_HEX
                + LXmlUtils.integerToXmlHexStr(c.getRed(), 2)
                + LXmlUtils.integerToXmlHexStr(c.getGreen(), 2)
                + LXmlUtils.integerToXmlHexStr(c.getBlue(), 2)
                : null);
    }

    public String toXmlStr() {
        return toXmlStr(this);
    }

    @Override
    public String toParseableString() {
        return "rgb(" + LString.concatWithComma(getRed(), getGreen(), getBlue()) + ")";
    }

    public static String toXmlStr(LColor c) {
        return (c != null ? Integer.toString(c.getRed()) + " "
                + Integer.toString(c.getGreen()) + " "
                + Integer.toString(c.getBlue())
                : null);
    }

    @Override
    public void parseXml(Node n, LXmlParseInfo xmlParseInfo) throws LParseException {
        if ((n.getTextContent() == null) || (LString.isEmpty(n.getTextContent().trim()))) {
            throw new LParseException(this, "Color value is empty.");
        }
        String sValue = LXmlUtils.prepareString(n.getTextContent());
        switch (sValue.charAt(0)) {
            case ILConstants.KEYWORD_HEX: {
                sValue = sValue.substring(1);
                while (sValue.length() < 6) {
                    sValue = "0" + sValue;
                }
                setValue(LXmlUtils.xmlStrToInteger(ILConstants.KEYWORD_HEX + sValue.substring(0, 2)),
                        LXmlUtils.xmlStrToInteger(ILConstants.KEYWORD_HEX + sValue.substring(2, 4)),
                        LXmlUtils.xmlStrToInteger(ILConstants.KEYWORD_HEX + sValue.substring(4, 6)),
                        (sValue.length() == 8 ? LXmlUtils.xmlStrToInteger(ILConstants.KEYWORD_HEX + sValue.substring(6, 8)) : 255));
                break;
            }
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9': {
                double[] co = LXmlUtils.xmlStrToDoubleArray(new StringBuilder(sValue), 3);

                /*int[] co = new int[3];
                for (int i = 0; i < 3; i++) {
                    co[i] = 0;
                }
                int i = sValue.indexOf(" ");
                int c = 0;
                while ((i > -1) && (c < 3)) {
                    co[c] = Integer.parseInt(sValue.substring(0, i));
                    sValue = sValue.substring(i + 1);
                    c++;
                    i = sValue.indexOf(" ");
                }
                if (c != 3) {
                    throw new NumberFormatException();
                }*/
                setValue((int) Math.round(co[0]), (int) Math.round(co[1]), (int) Math.round(co[2]), (co.length > 3 ? (int) Math.round(co[3]) : 255));
                break;
            }
            default: {
                boolean found = false;
                sValue = sValue.toLowerCase().trim();
                sValue = sValue.replace('_', '-');
                /*if (LBase.getBaseUI() != null) {
                    LColor c = LBase.getBaseUI().getUiColor(sValue);
                    if (c != null) {
                        value = c.value;                        
                        found = true;
                    }
                }*/
                if (!found) {
                    found = true;
                    switch (sValue) {
                        case "white" ->
                            value = WHITE.value;
                        case "light-gray" ->
                            value = LIGHT_GRAY.value;
                        case "gray" ->
                            value = GRAY.value;
                        case "dark-gray" ->
                            value = DARK_GRAY.value;
                        case "black" ->
                            value = BLACK.value;
                        case "red" ->
                            value = RED.value;
                        case "pink" ->
                            value = PINK.value;
                        case "orange" ->
                            value = ORANGE.value;
                        case "yellow" ->
                            value = YELLOW.value;
                        case "green" ->
                            value = GREEN.value;
                        case "magenta" ->
                            value = MAGENTA.value;
                        case "cyan" ->
                            value = CYAN.value;
                        case "blue" ->
                            value = BLUE.value;
                        case "transparent" ->
                            value = TRANSPARENT.value;
                        default ->
                            found = false;
                    }
                }
                if ((!found) && (SYSTEM_COLOR_PROVIDER != null)) {
                    Integer v = SYSTEM_COLOR_PROVIDER.apply(sValue);
                    found = (v != null);
                    value = (v != null ? v : value);
                }
                if (!found) {
                    LParseException pe = new LParseException(this, "Unknown color value: " + sValue);
                    LLog.error(this, pe.getMessage());
                    throw pe;
                }
            }
        }

    }

    @Override
    public void toXml(Document doc, Element node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int compareTo(LColor anotherColor) {
        if (anotherColor == null) {
            return 1;
        } else {
            return (value - anotherColor.value);
        }
    }

    @Override
    public boolean equals(Object anotherColor) {
        if ((anotherColor != null) && (anotherColor instanceof LColor)) {
            return (this.compareTo((LColor) anotherColor) == 0);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + this.value;
        return hash;
    }

    @Override
    public void parse(String value) throws LParseException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static int rgbValue(int r, int g, int b, int a) {
        testColorValueRange(r, g, b, a);
        return ((a & 0xFF) << 24)
                | ((b & 0xFF) << 16)
                | ((g & 0xFF) << 8)
                | ((r & 0xFF));

    }

}
