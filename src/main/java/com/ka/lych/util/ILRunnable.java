package com.ka.lych.util;

/**
 *
 * @author klausahrenberg
 * @param <R> result of this operation
 * @param <T> possible error of this operation
 */
@FunctionalInterface
public interface ILRunnable<R, T extends Throwable> {

    /**
     *
     * @param task
     * @return 
     * @throws T
     */
    public R run(LTask<R, T> task) throws T;

}
