package com.ka.lych.test;

import com.ka.lych.util.LFormula;

/**
 *
 * @author klausahrenberg
 */
public class LFormulaTest {    

    public static void main(String[] args) {
        var f = new LFormula("5 + 3 ");
        System.out.println("result of '" + f.getFormula() + "' is: "  + f.getValue());
        
        f = new LFormula("5 + 3 * 4");
        System.out.println("result of '" + f.getFormula() + "' is: "  + f.getValue());
        
        f = new LFormula("4(5 + 3) ");
        System.out.println("result of '" + f.getFormula() + "' is: "  + f.getValue());
    }
    
}
