package com.ethereum.utils;

@FunctionalInterface
public interface Consumer<T,E extends Throwable> {
    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    void accept(T t) throws E;
}
