package org.uulib.reckon.dsl

import java.util.concurrent.Callable

import org.uulib.reckon.strategy.BuildMetadataPartStrategy
import org.uulib.reckon.strategy.CompoundPreReleaseStrategy
import org.uulib.reckon.strategy.PreReleasePartStrategy
import org.uulib.util.Configurator

import groovy.transform.PackageScope
import groovy.transform.TupleConstructor

@TupleConstructor
@PackageScope final class CompoundPreReleaseCallable implements Callable<CompoundPreReleaseStrategy> {

	final Configurator<CompoundPreReleaseSpec> config

	@Override
	CompoundPreReleaseStrategy call() throws Exception {
		config.withConfigured({new CompoundPreReleaseSpec()}) { CompoundPreReleaseSpec spec ->
			return CompoundPreReleaseStrategy.builder()
					.setPreReleasePart(resolvePart('pre-release', PreReleasePartStrategy, spec.preRelease))
					.setBuildMetadataPart(resolvePart('build metadata', BuildMetadataPartStrategy, spec.buildMetadata))
					.build()
		}
	}

	private static <T> T resolvePart(String desc, Class<T> type, def part) {
		part = Util.extract(part)
		switch(part) {
			case type: return part
			case String: return PartStrategies.part(part)
			case null: return PartStrategies.none

			default:
				throw new IllegalStateException("Don't know how to interpret $desc part: $part")
		}
	}

}
