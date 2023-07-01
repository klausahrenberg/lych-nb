package com.ka.lych.observable;

import java.text.DecimalFormat;
import com.ka.lych.event.LObservableChangeEvent;
import com.ka.lych.geometry.LGeomUtils;
import com.ka.lych.util.LParseException;
import com.ka.lych.xml.LXmlUtils;

/**
 *
 * @author klausahrenberg
 */
public class LInteger extends LObservable<Integer> {

    protected Integer lowerLimit, upperLimit;    
    private DecimalFormat decimalFormat;
    private String pattern;

    private ILValidator<Integer> numberAcceptor = (LObservableChangeEvent<Integer> change) -> {
        return (!LGeomUtils.isWithinLimits(get(), lowerLimit, upperLimit) ?
                new LValueException(this, "Given value is out of limits: value=" + get() + "; lowerLimit=" + lowerLimit + "; upperLimit=" + upperLimit) :
                null);
    };
    
    public LInteger() {
        this(null, null, null);
    }

    public LInteger(Integer initialValue) {
        this(initialValue, null, null);
    }

    public LInteger(Integer initialValue, Integer lowerLimit, Integer upperLimit) {
        super(initialValue);
        initialize(initialValue, lowerLimit, upperLimit);
    }

    private void initialize(Integer initialValue, Integer lowerLimit, Integer upperLimit) {
        setLowerLimit(lowerLimit);
        setUpperLimit(upperLimit);
        pattern = "#,##0";  
        set(initialValue);        
    }

    /*@Override
    public boolean set(Integer value) {
        if (value != null) {
            if (!LGeomUtils.isWithinLimits(value, lowerLimit, upperLimit)) {
                if ((fitValueToLimits) && (lowerLimit != null) && (upperLimit != null)) {
                    double diff = upperLimit - lowerLimit;
                    if (diff <= 0.0) {
                        throw new IllegalArgumentException("Wrong range of limits, upperLimit has to be higher than lowerLimit: lowerLimit=" + lowerLimit + "; upperLimit=" + upperLimit);
                    }
                    value = (int) ((double) (value - lowerLimit) % diff);
                    value = (value < 0 ? upperLimit + value : lowerLimit + value);
                    return super.set(value);
                } else {
                    throw new IllegalArgumentException("Given value is out of limits: value=" + value + "; lowerLimit=" + lowerLimit + "; upperLimit=" + upperLimit);
                }
            } else if ((fitValueToLimits) && (lowerLimit != null) && (value == upperLimit)) {
                //special case: if fitValueToLimit and you give the max possible value, it will be lowered to the minimum value, e.g. 360° will be converted to 0°
                return super.set(lowerLimit);
            } else {
                return super.set(value);
            }
        } else {
            return super.set(null);
        } 
    } */  

    public Integer getLowerLimit() {
        return lowerLimit;
    }

    public void setLowerLimit(Integer lowerLimit) {
        if (this.lowerLimit != null) {
            this.removeAcceptor(numberAcceptor);
        }
        this.lowerLimit = lowerLimit;
        if (this.lowerLimit != null) {
            this.addAcceptor(numberAcceptor);
        }
    }

    public Integer getUpperLimit() {
        return upperLimit;
    }

    public void setUpperLimit(Integer upperLimit) {
        if (this.upperLimit != null) {
            this.removeAcceptor(numberAcceptor);
        }
        this.upperLimit = upperLimit;
        if (this.upperLimit != null) {
            this.addAcceptor(numberAcceptor);
        }
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        if ((this.pattern == null) || (!this.pattern.equals(pattern))) {            
            this.pattern = pattern;
            if (decimalFormat != null) {
                decimalFormat = new DecimalFormat(pattern);
            }
        }
    }
    
    public static LInteger clone(LInteger source) {
        LInteger result = null;
        if (source != null) {
            result = new LInteger(source.get(), source.getLowerLimit(), source.getUpperLimit());            
        }
        return result;
    }
    
    public void inc() {
        set(get() + 1);
    }
    
    @Override
    public void parse(String value) throws LParseException {
        try {
            setValue(LXmlUtils.xmlStrToInteger(value));
        } catch (LValueException lve) {
            throw new LParseException(this, lve.getMessage(), lve);
        }
    }

    @Override
    public void parseLocalized(String value) throws LParseException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toParseableString() {
        return toParseableString(get());
    }

    @Override
    public String toLocalizedString() {
        if (get() != null) {
            if (decimalFormat == null) {
                decimalFormat = new DecimalFormat(pattern);
            }
            return decimalFormat.format(get());
        } else {
            return null;
        }
    }
    
    public static String toParseableString(Integer value) {
        return (value != null ? Integer.toString(value) : null);
    }
    
    public static LInteger of(Integer value) {
        return new LInteger(value);
    }
    
    public static LInteger of(String value) throws LParseException {
        var result = new LInteger();
        result.parse(value);
        return result;
    }

}
