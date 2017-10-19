package org.uulib.reckon.util;

import java.util.Objects;
import java.util.function.Supplier;

public final class ConstantSupplier<T> implements Supplier<T> {
	
	private final T value;
	
	public static <T> ConstantSupplier<T> nonNull(T value) {
		return new ConstantSupplier<>(Objects.requireNonNull(value));
	}
	
	public static <T> ConstantSupplier<T> nullable(T value) {
		return new ConstantSupplier<>(value);
	}
	
	private ConstantSupplier(T value) {
		this.value = value;
	}

	@Override
	public T get() {
		return value;
	}

}
