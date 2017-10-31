package org.uulib.util;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Function;

import org.uulib.util.function.ExceptionalSupplier;

public final class ExceptionallyLazy<T, E extends Exception> implements ExceptionalSupplier<T, E> {
	
	private volatile ExceptionalSupplier<T,E> delegate;
	
	@SuppressWarnings("unchecked")
	public static <T> ExceptionallyLazy<T, Exception> ofCallable(Callable<? extends T> supplier) {
		return of((supplier instanceof ExceptionallyLazy<?,?>) ? (ExceptionalSupplier<T, Exception>) supplier :
			() -> supplier.call());	
	}
	
	public static <T,E extends Exception> ExceptionallyLazy<T,E> of(
			ExceptionalSupplier<? extends T, ? extends E> supplier) {
		return new ExceptionallyLazy<>(supplier);
	}
	
	private ExceptionallyLazy(ExceptionalSupplier<? extends T, ? extends E> supplier) {
		this.delegate = new DelegateSupplier(Objects.requireNonNull(supplier));
	}

	@Override
	public T get() throws E {
		return delegate.get();
	}
	
	@Override
	public <R> ExceptionallyLazy<R, E> map(Function<? super T, ? extends R> mapper) {
		return of(ExceptionalSupplier.super.map(mapper));
	}

	private class DelegateSupplier implements ExceptionalSupplier<T, E> {
		
		private ExceptionalSupplier<? extends T, ? extends E> supplier;
		
		DelegateSupplier(ExceptionalSupplier<? extends T, ? extends E> supplier) {
			this.supplier = () -> {
				try {
					T val = supplier.get();
					this.supplier = delegate = () -> val;
					return val;
				} catch (Exception el) {
					this.supplier = delegate = () -> {
						el.fillInStackTrace();
						throw el;
					};
					throw el;
				}
			};
		}

		@Override
		public synchronized T get() throws E {
			return supplier.get();
		}
		
	}
	
	

}
