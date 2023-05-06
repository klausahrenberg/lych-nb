package com.ka.lych.observable;

import com.ka.lych.util.LParseException;
import com.ka.lych.xml.LXmlUtils;
import com.ka.lych.util.LLog;

/**
 *
 * @author klausahrenberg
 */
public class LPixel extends LDouble {
    
    public final static String PX = "px";
    public final static String DP = "dp";
    
    public LPixel() {
    }

    public LPixel(Double initialValue) {
        super(initialValue);
    }
    
    public LPixel(String initialValue) {
        super();
        try {
            parse(initialValue);
        } catch (LParseException pex) {
            LLog.error(this, pex.getMessage(), pex);
        }    
    }
    
    /**
     * Parse a pixel value and scales it depending on current dpi value.
     * The value "10" will be interprated as "10dp", which means, it has 
     * to be dpi-scaled.
     * Use "10px" to use absolute pixel units. 
     * @param value 
     * @throws com.ka.lych.util.LParseException 
     */
    @Override
    public void parse(String value) throws LParseException {         
        value = value.trim().toLowerCase();
        if (!LString.isEmpty(value)) {
            String type = value.substring(value.length() - 2);
            switch (type) {
                case PX :
                    set(LXmlUtils.xmlStrToDouble(value.substring(0, value.length() - 2).trim()));
                    break;
                case DP : 
                    set(1.0f/*Base.getBaseUI().getDpiScale()*/ * LXmlUtils.xmlStrToDouble(value.substring(0, value.length() - 2).trim()));
                    break;
                default :    
                    //DP
                    set(1.0f/*LBase.getBaseUI().getDpiScale()*/ * LXmlUtils.xmlStrToDouble(value));
                    break;
            }
        } else {
            set(null);
        }
    }
    
    public static double getDefault(double pixel) {
        return pixel * 1.0f;//LBase.getBaseUI().getDpiScale();
    }

    @Override
    public void parseLocalized(String value) throws LParseException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toParseableString() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toLocalizedString() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
