package com.ka.lych.observable;

import com.ka.lych.exception.LParseException;
import com.ka.lych.geometry.ILBounds;
import com.ka.lych.xml.LXmlUtils;

/**
 *
 * @author klausahrenberg
 */
public class LObservableBounds extends LObservable<ILBounds, LObservableBounds> {

    public LObservableBounds() {
    }

    public LObservableBounds(ILBounds initialValue) {
        super(initialValue);
    }

    @Override
    public void parse(String value) throws LParseException {
        set(LXmlUtils.xmlStrToBounds(value));
    }

    @Override
    public String toParseableString() {
        return LXmlUtils.boundsToXmlStr(this.get());
    }

    @Override
    public LObservableBounds clone() throws CloneNotSupportedException {
        return new LObservableBounds(this.get());
    }
    
    
    
}
