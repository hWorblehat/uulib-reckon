package org.uulib.reckon.gradle;

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.uulib.reckon.dsl.Reckon

public class ReckonGradlePlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		def ext = project.extensions.create('reckon', ReckonExtension, project)
		project.version = ext.reckon({})
	}

}
	