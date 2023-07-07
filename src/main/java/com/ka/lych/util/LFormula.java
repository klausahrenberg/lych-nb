package com.ka.lych.util;

import com.ka.lych.list.LMap;
import com.ka.lych.observable.LString;
import java.util.function.Function;

/**
 * MFormula ermoeglicht das Parsen und rechnen von Formeln. Unterstuetzt werden
 * die Operationen: Summe, Produkt, Differenz, Division, Exponent Variablen
 * werden in einer Tabelle (variables) hinterlegt. Der Wert wird mit getValue()
 * abgefragt
 *
 * Jede Formel wird als Baum zerlegt. In jedem Schritt zerlegt das Object in die
 * Bestandteile value1 und value2, bis nur noch eine Variable oder Konstante
 * uebrig ist.
 *
 * @author Klaus Ahrenberg
 * @version 20070219 - erste Version uebernommen aus Delphi
 */
public class LFormula {

    public enum LOperation {
        SUM, DIFFERENCE, PRODUCT, QUOTIENT, EXPONENT
    }
    
    protected String formula = null;
    protected LOperation operation = LOperation.SUM;
    protected LFormula value1 = null;
    protected LFormula value2 = null;
    protected final static char[] operatordigits = {'+', '-', '*', '/', '^'};
    protected LMap<String, Double> variables = null;
    protected boolean alreadyCalculated = false;
    protected double value = 0;
    protected Function<String, Double> formulaListener;

    public LFormula() {
        this("");
    }

    /**
     * Erzeugt eine neue Instanz von MFormula.
     *
     * @param formula String, der die Formel repraesentiert
     */
    public LFormula(String formula) {
        this(formula, new LMap<String, Double>(), null);
    }

    public LFormula(String formula, LMap<String, Double> variables, Function<String, Double> formulaListener) {
        this.variables = variables;
        this.formulaListener = formulaListener;
        setFormula(formula);
    }

    private boolean checkIntegry(String value) {
        int informula = 0;
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) == '(') {
                informula++;
            }
            if ((value.charAt(i) == ')') && (informula > 0)) {
                informula--;
            }
        }
        return (informula == 0);
    }

    private boolean isOperator(char value) {
        for (int i = 0; i < operatordigits.length; i++) {
            if (operatordigits[i] == value) {
                return true;
            }
        }
        return false;
    }

    private boolean hasOperators(String value) {
        boolean result;
        result = ((value.contains("(")) || (value.contains(")")));
        if (!result) {
            for (int i = 0; i < operatordigits.length; i++) {
                if (value.indexOf(operatordigits[i]) > -1) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * gibt die Formel zurueck
     *
     * @return String, der die Formel repraesentiert
     */
    public String getFormula() {
        return formula;
    }

    /**
     * Setzt und parst die Formel Die Formel wird zun?chst formatiert und danach
     * geparst.
     *
     * @param newformula String, der die Formel repraesentiert
     */
    public void setFormula(String newformula) {
        this.setAlreadyCalculated(false);
        this.formula = newformula;

        //String decimalSeparator = new Character((new java.text.DecimalFormatSymbols()).getDecimalSeparator()).toString();
        //Leerzeichen entfernen
        formula = formula.replaceAll(" ", "");
        //':' ersetzen
        formula = formula.replaceAll(":", "/");

        //',' ersetzen
        //formula = formula.replaceAll(",", decimalSeparator);
        //'.' ersetzen
        //formula = formula.replaceAll("\\.", decimalSeparator);
        //'-(' als erste Zeichen ersetzen mit '-1*('
        if (formula.indexOf("-(") == 0) {
            formula = "-1*" + formula.substring(1, formula.length());
        }
        //Eine Zahl mit darauffolgender Klammer (z.b. 3(1+1)) als Produkt kennzeichnen: 3*(1+1)
        int i = formula.indexOf("(");
        if (i > 0) {
            try {
                if (Float.valueOf(formula.substring(0, i)) != null) {
                    formula = formula.substring(0, i) + "*" + formula.substring(i, formula.length());
                }
            } catch (Exception e) {
            }
        }
        //Moegliche aeussere Klammern entfernen
        while ((formula.length() > 1)
                && (formula.charAt(0) == '(') && (formula.charAt(formula.length() - 1) == ')')
                && (checkIntegry(formula.substring(1, formula.length() - 1)))) {
            formula = formula.substring(1, formula.length() - 1);
        }
        parse();
    }

    /**
     * Zerlegt die Formel in eine Baumstruktur
     *
     * @return gibt an, ob das Zerlegen der Formel erfolgreich war
     */
    public boolean parse() {
        boolean result = false;
        value1 = null;
        value2 = null;
        operation = LOperation.SUM;
        if (!LString.isEmpty(formula)) {
            //Zuerst wird geprueft, ob es sich um einen Wert handelt
            result = false;
            try {
                result = (Float.valueOf(formula) != null);
            } catch (Exception e) {
            }
            //Falls dies erfolglos war, wird untersucht, ob es eine variable ist
            if (!result) {
                result = (!hasOperators(formula));
            }
            //Summe bzw. Differenz pruefen
            if (!result) {
                int informula = 0;
                for (int i = 0; i < formula.length(); i++) {
                    if (formula.charAt(i) == '(') {
                        informula++;
                    }
                    if (formula.charAt(i) == ')') {
                        informula--;
                    }
                    if (((formula.charAt(i) == '+') || (formula.charAt(i) == '-'))
                            && (informula == 0) && (i > 0)
                            && (!isOperator(formula.charAt(i - 1)))) {
                        if (formula.charAt(i) == '-') {
                            operation = LOperation.DIFFERENCE;
                        }
                        value1 = new LFormula(formula.substring(0, i), variables, formulaListener);
                        value2 = new LFormula(formula.substring(i + 1, formula.length()), variables, formulaListener);
                        result = true;
                        break;
                    }
                }
            }            
            //Quotient pruefen
            if ((!result) && (value1 == null) && (value2 == null)) {
                operation = LOperation.QUOTIENT;
                int informula = 0;
                for (int i = 0; i < formula.length(); i++) {
                    if (formula.charAt(i) == '(') {
                        informula++;
                    }
                    if (formula.charAt(i) == ')') {
                        informula--;
                    }
                    if ((formula.charAt(i) == '/')
                            && (informula == 0) && (i > 0)) {
                        value1 = new LFormula(formula.substring(0, i), variables, formulaListener);
                        value2 = new LFormula(formula.substring(i + 1, formula.length()), variables, formulaListener);
                        result = true;
                        break;
                    }
                }
            }
            //Exponent pruefen
            if ((!result) && (value1 == null) && (value2 == null)) {
                operation = LOperation.EXPONENT;
                int informula = 0;
                for (int i = 0; i < formula.length(); i++) {
                    if (formula.charAt(i) == '(') {
                        informula++;
                    }
                    if (formula.charAt(i) == ')') {
                        informula--;
                    }
                    if ((formula.charAt(i) == '^')
                            && (informula == 0) && (i > 0)) {
                        value1 = new LFormula(formula.substring(0, i), variables, formulaListener);
                        value2 = new LFormula(formula.substring(i + 1, formula.length()), variables, formulaListener);
                        result = true;
                        break;
                    }
                }
            }
            //Produkt pruefen
            if ((!result) && (value1 == null) && (value2 == null)) {
                operation = LOperation.PRODUCT;
                int informula = 0;
                for (int i = 0; i < formula.length(); i++) {
                    if (formula.charAt(i) == '(') {
                        informula++;
                    }
                    if (formula.charAt(i) == ')') {
                        informula--;
                    }
                    if ((formula.charAt(i) == '*') && (informula == 0) && (i > 0)) {
                        value1 = new LFormula(formula.substring(0, i), variables, formulaListener);
                        value2 = new LFormula(formula.substring(i + 1, formula.length()), variables, formulaListener);
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * rechnet das Ergebnis der Formel aus.
     *
     * @return Ergebnis als double
     */
    public double getValue() {
        if (!alreadyCalculated) {
            value = 0;
            if ((value1 != null) && (value2 != null)) {
                switch (operation) {
                    case DIFFERENCE ->
                        value = value1.getValue() - value2.getValue();
                    case PRODUCT ->
                        value = value1.getValue() * value2.getValue();
                    case QUOTIENT ->
                        value = value1.getValue() / value2.getValue();
                    case EXPONENT ->
                        value = Math.pow(value1.getValue(), value2.getValue());
                    default ->
                        value = value1.getValue() + value2.getValue();
                }
            } else {
                try {
                    //Konstante
                    value = Float.valueOf(formula).doubleValue();
                } catch (Exception e) {
                    //Variable                    
                    if (formulaListener != null) {
                        value = formulaListener.apply(formula);
                    } else {
                        var v = variables.get(formula);
                        if (v == null) {
                            throw new IllegalArgumentException("Can't get value for variable: " + formula);
                        }
                        value = v.doubleValue();
                    }
                }
            }
            alreadyCalculated = true;
        }
        return value;
    }

    /**
     * Zugrif auf die Liste der Variablen. Mit <CODE>put()</CODE> koennen
     * weitere Varaiblen hinzugefuegt werden. z.B:
     * <CODE>put("PI", new Double(3.141592654))</CODE>
     *
     * @return Liste
     */
    public LMap<String, Double> getVariables() {
        return variables;
    }

    public Function<String, Double> getFormulaListener() {
        return formulaListener;
    }

    public void setFormulaListener(Function<String, Double> formulaListener) {
        this.formulaListener = formulaListener;
        if (value1 != null) {
            value1.setFormulaListener(formulaListener);
        }
        if (value2 != null) {
            value2.setFormulaListener(formulaListener);
        }
    }

    public boolean isAlreadyCalculated() {
        return alreadyCalculated;
    }

    public void setAlreadyCalculated(boolean alreadyCalculated) {
        this.alreadyCalculated = alreadyCalculated;
        if (value1 != null) {
            this.value1.setAlreadyCalculated(alreadyCalculated);
        }
        if (value2 != null) {
            this.value2.setAlreadyCalculated(alreadyCalculated);
        }
    }

}
