package com.ka.lych.repo.sql;

/**
 *
 * @author klausahrenberg
 * @param <T>
 * @param <U>
 * @param <V>
 * @param <R>
 */
public interface ILSqlConverter<T, U, V, R> {

    /**
     * Applies this function to the given arguments.
     *
     * @param t the first function argument
     * @param u the second function argument
     * @param v the third function argument
     * @return the function result
     */
    R apply(T t, U u, V v);
}    