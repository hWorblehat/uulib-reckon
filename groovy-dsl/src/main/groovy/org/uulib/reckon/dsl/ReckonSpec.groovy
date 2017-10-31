package org.uulib.reckon.dsl

import java.util.function.Consumer

import org.uulib.dsl.basedon.BasedOn
import org.uulib.util.Configurator

import groovy.transform.PackageScope

class ReckonSpec implements VersionStrategies, CompoundStrategies, PartStrategies, BasedOn, VcsInventories {

	def vcs
	def normalVersion
	def preReleaseVersion
	
	@PackageScope ReckonSpec() {}
	
	void preReleaseVersion(Consumer<CompoundPreReleaseSpec> config) {
		preReleaseVersion = compound(config)
	}
	
	void preReleaseVersion(Closure config) {
		preReleaseVersion = compound(config)
	}
	
}
