package org.uulib.reckon.dsl

import java.util.concurrent.Callable
import java.util.function.Consumer

import org.uulib.reckon.strategy.CompoundPreReleaseStrategy
import org.uulib.util.Configurator

trait CompoundStrategies {
	
	static Callable<CompoundPreReleaseStrategy> compound(Consumer<CompoundPreReleaseSpec> config) {
		new CompoundPreReleaseCallable(Configurator.using(config))
	}
	
	static Callable<CompoundPreReleaseStrategy> compound(Closure config) {
		new CompoundPreReleaseCallable(Configurator.using(config))
	}

}
