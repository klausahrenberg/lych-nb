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
public class LDouble extends LObservable<Double> {

    protected Double lowerLimit, upperLimit;
    protected double precision;    
    private DecimalFormat decimalFormat;
    private String pattern;
    
    private ILValidator<Double> numberAcceptor = (LObservableChangeEvent<Double> change) -> {
        return (!LGeomUtils.isWithinLimits(get(), lowerLimit, upperLimit, precision * 0.001) ?
                new LValueException(this, "Given value is out of limits: value=" + get() + "; lowerLimit=" + lowerLimit + "; upperLimit=" + upperLimit) :
                null);
    };

    public LDouble() {
        this(null, null, null);
    }

    public LDouble(Double initialValue) {
        this(initialValue, null, null);
    }    

    public LDouble(Double initialValue, Double lowerLimit, Double upperLimit) {
        super();
        initialize(initialValue, lowerLimit, upperLimit);        
    }

    private void initialize(Double initialValue, Double lowerLimit, Double upperLimit) {
        setLowerLimit(lowerLimit);
        setUpperLimit(upperLimit);
        precision = 0.01;
        pattern = "#,##0.00";        
        set(initialValue);
    }

    /*@Override
    public boolean set(Double newValue) {
        if (newValue != null) {
            if (!LGeomUtils.isWithinLimits(newValue, lowerLimit, upperLimit, precision * 0.001)) {
                if ((fitValueToLimits) && (lowerLimit != null) && (upperLimit != null)) {
                    double diff = upperLimit - lowerLimit;
                    if (diff <= 0.0) {
                        throw new IllegalArgumentException("Wrong range of limits, upperLimit has to be higher than lowerLimit: lowerLimit=" + lowerLimit + "; upperLimit=" + upperLimit);
                    }
                    newValue = (newValue - lowerLimit) % diff;
                    newValue = (newValue < 0 ? upperLimit + newValue : lowerLimit + newValue);
                    return super.set(newValue);
                } else {
                    throw new IllegalArgumentException("Given value is out of limits: value=" + newValue + "; lowerLimit=" + lowerLimit + "; upperLimit=" + upperLimit);
                }
            } else if ((fitValueToLimits) && (lowerLimit != null) && (LGeomUtils.isEqual(newValue, upperLimit, precision * 0.001))) {
                //special case: if fitValueToLimit and you give the max possible value, it will be lowered to the minimum value, e.g. 360° will be converted to 0°
                return super.set(lowerLimit);
            } else {
                return super.set(newValue);
            }
        } else {
            return super.set(null);
        }
    }*/

    public double getPrecision() {
        return precision;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    public Double getLowerLimit() {
        return lowerLimit;
    }

    public void setLowerLimit(Double lowerLimit) {
        if (this.lowerLimit != null) {
            this.removeAcceptor(numberAcceptor);
        }
        this.lowerLimit = lowerLimit;
        if (this.lowerLimit != null) {
            this.addAcceptor(numberAcceptor);
        }
    }

    public Double getUpperLimit() {
        return upperLimit;
    }

    public void setUpperLimit(Double upperLimit) {
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

    public static LDouble clone(LDouble source) {
        LDouble result = null;
        if (source != null) {
            if (source.isBoundInAnyWay()) {
                throw new IllegalStateException("Bounded observable can't be cloned.");
            }
            result = new LDouble(source.get(), source.getLowerLimit(), source.getUpperLimit());
            result.setPrecision(source.getPrecision());
        }
        return result;
    }

    @Override
    public void parse(String value) throws LParseException {
        set(LXmlUtils.xmlStrToDouble(value));
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
    
    public static String toParseableString(Double value) {
        return (value != null ? Double.toString(value) : null);
    }
    
    public static LDouble of(String value) throws LParseException {
        var result = new LDouble();
        result.parse(value);
        return result;
    }
    
    public static LDouble of(double value) {
        return new LDouble(value);
    }

}
