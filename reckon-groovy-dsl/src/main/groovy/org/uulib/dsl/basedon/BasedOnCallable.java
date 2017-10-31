package org.uulib.dsl.basedon;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import org.uulib.util.Configurator;

class BasedOnCallable<S,T> implements Callable<T> {
	private final Supplier<Optional<S>> basis;
	private final Configurator<BasedOnSpec<S, T>> configurator;

	BasedOnCallable(Supplier<Optional<S>> basis, Configurator<BasedOnSpec<S, T>> configurator) {
		this.basis = basis;
		this.configurator = configurator;
	}

	@Override
	public T call() throws Exception {
		return configurator.withConfigured(BasedOnSpec::new, config -> basis.get()
				.map(s -> Optional.ofNullable(config.mappings.get(s)))
				.orElse(config.whenAbsent)
				.orElseGet(() -> config.normal.orElseThrow(IllegalStateException::new))
				.call());
	}

}