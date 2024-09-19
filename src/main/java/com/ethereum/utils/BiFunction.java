package com.ethereum.utils;

@FunctionalInterface
public interface BiFunction<T,U,R,E extends Throwable> {
    /**
     * Applies this function to the given arguments.
     *
     * @param t the first function argument
     * @param u the second function argument
     * @return the function result
     */
    R apply(T t, U u) throws E;

}
