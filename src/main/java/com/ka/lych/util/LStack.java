/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ka.lych.util;

import java.util.EmptyStackException;
import java.util.Stack;

/**
 *
 * @author klausahrenberg
 * @param <E>
 */
public class LStack<E> extends Stack<E> {

    private final boolean _lifo;

    public LStack(boolean useLIFO) {
        this._lifo = useLIFO;
    }

    @Override
    public synchronized E peek() {
        int len = size();
        if (len == 0) {
            throw new EmptyStackException();
        }
        if (_lifo) {
            return elementAt(len - 1);
        } else {
            return elementAt(0);
        }
    }

    @Override
    public synchronized E pop() {
        E obj = peek();
        if (_lifo) {
            removeElementAt(size() - 1);
        } else {
            removeElementAt(0);
        }
        return obj;
    }

}
