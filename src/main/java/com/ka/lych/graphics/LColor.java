package com.ka.lych.graphics;

import com.ka.lych.exception.LException;
import com.ka.lych.list.LMap;
import com.ka.lych.observable.LString;
import com.ka.lych.util.ILConstants;
import com.ka.lych.util.ILParseable;
import com.ka.lych.util.LReflections;
import com.ka.lych.xml.LXmlUtils;

/**
 *
 * @author klausahrenberg
 */
public class LColor
        implements /*ILXmlSupport,*/ Comparable<LColor>, ILParseable {

    public final static LColor BLACK = new LColor(0, 0, 0);
    public final static LColor WHITE = new LColor(255, 255, 255);
    public final static LColor SILVER = new LColor(192, 192, 192);
    public final static LColor GRAY = new LColor(128, 128, 128);
    public final static LColor MAROON = new LColor(128, 0, 0);
    public final static LColor RED = new LColor(255, 0, 0);
    public final static LColor PURPLE = new LColor(128, 0, 128);
    public final static LColor FUCHSIA = new LColor(255, 0, 255);
    public final static LColor GREEN = new LColor(0, 128, 0);
    public final static LColor LIME = new LColor(0, 255, 0);
    public final static LColor OLIVE = new LColor(128, 128, 0);
    public final static LColor YELLOW = new LColor(255, 255, 0);
    public final static LColor NAVY = new LColor(0, 0, 128);
    public final static LColor BLUE = new LColor(0, 0, 255);
    public final static LColor TEAL = new LColor(0, 128, 128);
    public final static LColor AQUA = new LColor(0, 255, 255);

    public final static LColor LIGHTGRAY = new LColor(192, 192, 192);
    public final static LColor DARKGRAY = new LColor(64, 64, 64);
    public final static LColor PINK = new LColor(255, 175, 175);
    public final static LColor ORANGE = new LColor(255, 200, 0);
    public final static LColor MAGENTA = new LColor(255, 0, 255);
    public final static LColor CYAN = new LColor(0, 255, 255);
    public final static LColor TRANSPARENT = new LColor(0, 0, 0, 0);
    public final static LColor NONE = TRANSPARENT;

    static LMap<String, LColor> _PREDEFINED_COLORS;

    int _rgb;

    public LColor() {
        this(0, 0, 0, 0);
    }

    public LColor(int rgbValue) {
        _rgb = rgbValue;
    }

    public LColor(int r, int g, int b) {
        this(r, g, b, 255);
    }

    public LColor(int r, int g, int b, int a) {
        rgb(r, g, b, a);
    }

    private LColor rgb(int r, int g, int b, int a) {
        _rgb = LColor.rgbOf(r, g, b, a);
        return this;
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

    public int rgb() {
        return _rgb;
    }

    public int red() {
        return (rgb() >> 0) & 0xFF;
    }

    public int green() {
        return (rgb() >> 8) & 0xFF;
    }

    public int blue() {
        return (rgb() >> 16) & 0xFF;
    }

    public int alpha() {
        return (rgb() >> 24) & 0xff;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[r=" + red() + ", g=" + green() + ", b=" + blue() + ", a=" + alpha() + "]";
    }

    public String toXmlHexStr() {
        return toXmlHexStr(this);
    }

    public static String toXmlHexStr(LColor c) {
        return (c != null
                ? ILConstants.KEYWORD_HEX
                + LXmlUtils.integerToXmlHexStr(c.red(), 2)
                + LXmlUtils.integerToXmlHexStr(c.green(), 2)
                + LXmlUtils.integerToXmlHexStr(c.blue(), 2)
                : null);
    }

    public String toXmlStr() {
        return toXmlStr(this);
    }

    @Override
    public String toParseableString() {
        return "rgb(" + LString.concatWithComma(red(), green(), blue()) + ")";
    }

    public static String toXmlStr(LColor c) {
        return (c != null ? Integer.toString(c.red()) + " "
                + Integer.toString(c.green()) + " "
                + Integer.toString(c.blue())
                : null);
    }

    @Override
    public int compareTo(LColor anotherColor) {
        if (anotherColor == null) {
            return 1;
        } else {
            return (_rgb - anotherColor.rgb());
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
        hash = 89 * hash + _rgb;
        return hash;
    }

    @Override
    public void parse(String value) throws LException {
        _rgb = LColor.of(value).rgb();
    }

    public static int rgbOf(int r, int g, int b, int a) {
        testColorValueRange(r, g, b, a);
        return ((a & 0xFF) << 24)
                | ((b & 0xFF) << 16)
                | ((g & 0xFF) << 8)
                | ((r & 0xFF));

    }

    public static LColor of(int r, int g, int b) {
        return of(r, g, b, 255);
    }

    public static LColor of(int r, int g, int b, int a) {
        return new LColor(r, g, b, a);
    }

    public static LColor of(String sValue) throws LException {
        sValue = sValue.trim().replace(" ", "").toLowerCase();
        if ((sValue.startsWith("rgb(")) && (sValue.endsWith(")"))) {
            sValue = sValue.substring(4, sValue.length() - 1);
        }
        switch (sValue.charAt(0)) {
            case ILConstants.KEYWORD_HEX -> {
                sValue = sValue.substring(1);
                while (sValue.length() < 6) {
                    sValue = "0" + sValue;
                }
                return new LColor(LXmlUtils.xmlStrToInteger(ILConstants.KEYWORD_HEX + sValue.substring(0, 2)),
                        LXmlUtils.xmlStrToInteger(ILConstants.KEYWORD_HEX + sValue.substring(2, 4)),
                        LXmlUtils.xmlStrToInteger(ILConstants.KEYWORD_HEX + sValue.substring(4, 6)),
                        (sValue.length() == 8 ? LXmlUtils.xmlStrToInteger(ILConstants.KEYWORD_HEX + sValue.substring(6, 8)) : 255));
            }
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                double[] co = LXmlUtils.xmlStrToDoubleArray(new StringBuilder(sValue), 3);
                return new LColor((int) Math.round(co[0]), (int) Math.round(co[1]), (int) Math.round(co[2]), (co.length > 3 ? (int) Math.round(co[3]) : 255));
            }
            default -> {
                LColor.createPredefinedColors();
                LColor result = _PREDEFINED_COLORS.get(sValue.replace("-", "").replace("_", ""));
                if (result != null) {
                    return result;
                } else {
                    throw new LException("Unknown color value: %s", sValue);
                }
            }

        }
    }

    protected static void createPredefinedColors() {
        if (_PREDEFINED_COLORS == null) {
            _PREDEFINED_COLORS = new LMap<String, LColor>();
            var fields = LReflections.getStaticFields(LColor.class, LColor.class, (Class) null);
            fields.forEach(field -> {
                try {
                    _PREDEFINED_COLORS.put(field.name().toLowerCase(), (LColor) field.get(null));
                } catch (Exception ex) {
                }
            });
        }
    }
    
    public static void addPredefinedColor(String colorName, LColor color) {
        LColor.createPredefinedColors();
        _PREDEFINED_COLORS.put(colorName.replace("-", "").replace("_", "").toLowerCase(), color);
    }
    
    public static void addPredefinedColors(LMap<String, LColor> colors) {
        LColor.createPredefinedColors();
        _PREDEFINED_COLORS.putAll(colors);
    }

}
