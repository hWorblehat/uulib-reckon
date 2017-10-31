package org.uulib.reckon.dsl

import org.uulib.dsl.basedon.BasedOn

import groovy.transform.PackageScope

class CompoundPreReleaseSpec implements PartStrategies, BasedOn {
	
	def preRelease
	def buildMetadata
	
	@PackageScope CompoundPreReleaseSpec() {}

}
