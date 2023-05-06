/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ka.lych.test;

import com.ka.lych.observable.LString;

/**
 *
 * @author klausahrenberg
 */
public class LTest {

    private final String DEFAULT_DUUPI = null;
    
    private LString duupi;

    public LString observableDuupi() {
        if (duupi == null) {
            duupi = new LString(DEFAULT_DUUPI);
        }
        return duupi;
    }

    public String getDuupi() {
        return duupi != null ? duupi.get() : DEFAULT_DUUPI;
    }

    public void setDuupi(String duupi) {
        observableDuupi().set(duupi);
    }
    
}
