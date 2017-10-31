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

class Reckon {
	
	private static ReckonedVersion doReckon(Configurator<ReckonSpec> config) {
		return new ReckonedVersion(new ReckonCallable(config))
	}
	
	static ReckonedVersion reckon(Consumer<ReckonSpec> config) {
		return doReckon(Configurator.using(config))
	}
	
	static ReckonedVersion reckon(Closure config) {
		return doReckon(Configurator.using(config))
	}
	
	@TupleConstructor(includeFields=true)
	private static final class ReckonCallable implements Callable<Version> {
		
		private final Configurator<ReckonSpec> config;

		@Override
		public Version call() throws Exception {
			config.withConfigured({new ReckonSpec()}) { ReckonSpec spec ->
				return Reckoner.reckon(
					resolveVcsInventory(spec.vcs),
					resolveNormalStrategy(spec.normalVersion),
					resolvePreReleaseStrategy(spec.preReleaseVersion)
				)
			}
		}
		
		private static VcsInventorySupplier resolveVcsInventory(def vcsInventory) {
			vcsInventory = Util.extract(vcsInventory)
			switch(vcsInventory) {
				case VcsInventorySupplier: return vcsInventory
				case VcsInventory: return {vcsInventory} as VcsInventorySupplier
				
				case null: throw new IllegalStateException('VCS inventory not set.')
				default: throw new IllegalStateException(
					"Don't know how to interpret VCS inventory: $vcsInventory")
			}
		}
		
		private static NormalStrategy resolveNormalStrategy(def normalStrategy) {
			normalStrategy = Util.extract(normalStrategy)
			switch(normalStrategy) {
				case NormalStrategy: return normalStrategy
				case Integer:
				case String:
				case Version:
					return VersionStrategies.version(normalStrategy)
					
				case null: throw new IllegalStateException('Normal strategy not set.')
				default: throw new IllegalStateException(
						"Don't know how to interpret normal strategy: $normalStrategy")
			}
		}
		
		private static PreReleaseStrategy resolvePreReleaseStrategy(def preReleaseStrategy) {
			preReleaseStrategy = Util.extract(preReleaseStrategy)
			switch(preReleaseStrategy) {
				case PreReleaseStrategy: return preReleaseStrategy
				
				case String: preReleaseStrategy = PartStrategies.preRelease(preReleaseStrategy) // v Fall through v
				case PreReleasePartStrategy: return CompoundPreReleaseStrategy.builder()
						.setPreReleasePart(preReleaseStrategy)
						.build()
				
				case null: return VersionStrategies.none
				
				default: throw new IllegalStateException(
						"Don't know how to interpret pre-release strategy: $preReleaseStrategy")
			}
		}
		
	}

}
