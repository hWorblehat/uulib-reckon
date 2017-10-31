package org.uulib.reckon.gradle

import javax.inject.Inject

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.uulib.dsl.basedon.BasedOn
import org.uulib.reckon.dsl.CompoundStrategies
import org.uulib.reckon.dsl.Reckon
import org.uulib.reckon.dsl.ReckonedVersion
import org.uulib.reckon.dsl.VcsInventories
import org.uulib.reckon.dsl.VersionStrategies

class ReckonExtension implements VersionStrategies, CompoundStrategies, BasedOn, VcsInventories {
	
	final Property<Object> vcs
	final Property<Object> normalVersion
	final Property<Object> preReleaseVersion
	
	@Inject
	ReckonExtension(ObjectFactory objects) {
		vcs = objects.property(Object)
		normalVersion = objects.property(Object)
		preReleaseVersion = objects.listProperty(Object)
	}
	
	ReckonedVersion reckon(Closure closure) {
		Reckon.reckon(closure >> {
			for(prop in ['vcs', 'normalVersion', 'preReleaseVersion']) {
				if(delegate."${prop}"==null) {
					delegate."${prop}" = this."${prop}".orNull
				}
			}
		})
	}
	
}
