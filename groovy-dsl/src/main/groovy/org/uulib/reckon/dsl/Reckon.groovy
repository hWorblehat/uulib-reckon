package org.uulib.reckon.dsl

import java.util.concurrent.Callable
import java.util.function.Consumer

import org.ajoberstar.reckon.core.NormalStrategy
import org.ajoberstar.reckon.core.PreReleaseStrategy
import org.ajoberstar.reckon.core.VcsInventory
import org.ajoberstar.reckon.core.VcsInventorySupplier
import org.uulib.reckon.Reckoner
import org.uulib.reckon.strategy.CompoundPreReleaseStrategy
import org.uulib.reckon.strategy.PreReleasePartStrategy
import org.uulib.util.Configurator

import com.github.zafarkhaja.semver.Version

import groovy.transform.TupleConstructor

final class Reckon {
	
	private Reckon() {}
	
	private static ReckonedVersion doReckon(Configurator<ReckonSpec> config) {
		return new ReckonedVersion(new ReckonCallable(config))
	}
	
	static ReckonedVersion reckon(Consumer<ReckonSpec> config) {
		return doReckon(Configurator.using(config))
	}
	
	static ReckonedVersion reckon(Closure config) {
		return doReckon(Configurator.using(config))
	}

}
