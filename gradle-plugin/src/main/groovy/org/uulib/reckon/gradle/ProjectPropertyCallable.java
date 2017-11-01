package org.uulib.reckon.gradle;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import org.gradle.api.Project;

public final class ProjectPropertyCallable implements Callable<String> {
	
	private final Project project;
	private final String[] propertyNames;
	private final String defaultValue;
	
	public ProjectPropertyCallable(Project project, String propertyName) {
		this(project, new String[] {propertyName}, null);
	}
	
	private ProjectPropertyCallable(Project project, String[] propertyNames, String defaultValue) {
		this.project = project;
		this.propertyNames = propertyNames;
		this.defaultValue = defaultValue;
	}
	
	public ProjectPropertyCallable or(String propertyName) {
		String[] newNames = Arrays.copyOf(propertyNames, propertyNames.length + 1);
		newNames[propertyNames.length] = propertyName;
		return new ProjectPropertyCallable(project, newNames, defaultValue);
	}
	
	public ProjectPropertyCallable orElse(String defaultValue) {
		return new ProjectPropertyCallable(project, propertyNames, defaultValue);
	}

	@Override
	public String call() throws Exception {
		return Stream.of(propertyNames)
				.filter(project::hasProperty)
				.findFirst()
				.map(s -> project.property(s).toString())
				.orElse(defaultValue);
	}

}
