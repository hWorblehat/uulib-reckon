package org.uulib.reckon.gradle

import java.util.concurrent.Callable

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.uulib.dsl.basedon.BasedOn
import org.uulib.reckon.dsl.CompoundStrategies
import org.uulib.reckon.dsl.PartStrategies
import org.uulib.reckon.dsl.Reckon
import org.uulib.reckon.dsl.ReckonedVersion
import org.uulib.reckon.dsl.VcsInventories
import org.uulib.reckon.dsl.VersionStrategies

class ReckonExtension implements VersionStrategies, CompoundStrategies, BasedOn, VcsInventories, PartStrategies {
	
	private final Project project;
	
	final Property<Object> vcs
	final Property<Object> normalVersion
	final Property<Object> preReleaseVersion
	
	ReckonExtension(Project project) {
		this.project = project
		vcs = project.objects.property(Object)
		normalVersion = project.objects.property(Object)
		preReleaseVersion = project.objects.property(Object)
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
	
	Callable<String> projectProperty(String propertyName) {
		return new ProjectPropertyCallable(project, propertyName)
	}
	
}
