package com.ethereum.utils;

@FunctionalInterface
public interface ConsumerT<T,V extends Throwable> {
    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    void accept(T t) throws V;
}
