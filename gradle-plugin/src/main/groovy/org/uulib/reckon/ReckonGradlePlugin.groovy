package org.uulib.reckon;

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.uulib.reckon.dsl.Reckon
import org.uulib.reckon.gradle.ReckonExtension

public class ReckonGradlePlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		def ext = project.extensions.create('reckon', ReckonExtension)
		project.version = ext.reckon({})
	}

}
	