package org.uulib.dsl.basedon;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.uulib.util.Configurator;
import org.uulib.util.ConstantSupplier;
import org.uulib.util.SneakySupplier;

import groovy.lang.Closure;

public interface BasedOn {

	static final AbsentSingleton absent = new AbsentSingleton();

	static <S, T> Callable<T> basedOn(Supplier<Optional<S>> basis, Consumer<BasedOnSpec<S, T>> configurator) {
		return new BasedOnCallable<>(basis, Configurator.using(configurator));
	}

	static <S, T> Callable<T> basedOn(Callable<S> basis, Consumer<BasedOnSpec<S,T>> configurator) {
		return basedOn(new SneakySupplier<>(() -> Optional.ofNullable(basis.call())), configurator);
	}

	static <S, T> T basedOn(Optional<S> basis, Consumer<BasedOnSpec<S, T>> configurator) throws Exception {
		return basedOn(ConstantSupplier.nonNull(basis), configurator).call();
	}

	static <S, T> T basedOn(S basis, Consumer<BasedOnSpec<S, T>> configurator) throws Exception {
		return basedOn(Optional.ofNullable(basis), configurator);
	}

	static <S, T> Callable<T> basedOn(Supplier<Optional<S>> basis, Closure<?> configurator) {
		return new BasedOnCallable<>(basis, Configurator.using(configurator));
	}

	static <S, T> Callable<T> basedOn(Callable<S> basis, Closure<?> configurator) {
		return basedOn(new SneakySupplier<>(() -> Optional.ofNullable(basis.call())), configurator);
	}

	static <S, T> T basedOn(Optional<S> basis, Closure<?> configurator) throws Exception {
		return BasedOn.<S,T>basedOn(ConstantSupplier.nonNull(basis), configurator).call();
	}

	static <S, T> T basedOn(S basis, Closure<?> configurator) throws Exception {
		return basedOn(Optional.ofNullable(basis), configurator);
	}

	static final class AbsentSingleton {
		private AbsentSingleton() {}
	}

}