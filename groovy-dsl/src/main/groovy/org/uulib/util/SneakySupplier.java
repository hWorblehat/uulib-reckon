package org.uulib.util;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

public class SneakySupplier<T> implements Supplier<T> {
	private final Callable<T> delegate;
	
	public SneakySupplier(Callable<T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public T get() {
		try {
			return delegate.call();
		} catch (Exception e) {
			throw (RuntimeException) e;
		}
	}
	
}