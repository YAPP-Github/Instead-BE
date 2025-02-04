package org.snsclient.twitter.exception;

@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Exception> {
	R apply(T t) throws E;
}
