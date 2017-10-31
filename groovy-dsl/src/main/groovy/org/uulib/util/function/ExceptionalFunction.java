package org.uulib.util.function;

import java.util.function.Function;

@FunctionalInterface
public interface ExceptionalFunction<I,O,E extends Exception> {
	
	O apply(I input) throws E;
	
	default <T> ExceptionalFunction<I, T, E> andThen(Function<O,T> mapper) {
		return (input) -> mapper.apply(apply(input));
	}
	
	default <T> ExceptionalFunction<I,T,E> andThen(ExceptionalFunction<O, T, ? extends E> mapper) {
		return (input) -> mapper.apply(apply(input));
	}

}
