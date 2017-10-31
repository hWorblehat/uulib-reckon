package org.uulib.util.function;

import java.util.concurrent.Callable;
import java.util.function.Function;

public interface ExceptionalSupplier<T, E extends Exception> extends Callable<T> {
	
	T get() throws E;

	@Override
	default T call() throws E { return get(); }
	
	default <R> ExceptionalSupplier<R, E> map(Function<? super T, ? extends R> mapper) {
		return () -> mapper.apply(get());
	}
}