package org.uulib.reckon.gradle

import java.util.List
import java.util.regex.Pattern

import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.Person
import org.eclipse.jgit.api.Git
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder

import spock.lang.*

import static org.gradle.testkit.runner.TaskOutcome.*

class ReckonGradlePluginSpec extends Specification {
	
	@Shared Person mrBlobby = new Person(name: 'Mr Blobby', email: 'blobby@noelshouseparty.tv')

	@Shared List<String> compatibleGradleVersions =
	System.getProperty('org.uulib.compatibleGradleVersions').split(',')

	@Rule TemporaryFolder projectDir = new TemporaryFolder()
	Grgit grgit

	def setup() {
		grgit = Grgit.init(dir: projectDir.root)

		projectDir.newFile('build.gradle') << '''\
plugins {
	id 'org.uulib.reckon'
	id 'org.ajoberstar.grgit'
}

if(project.status=='release') {
	project.status = 'development'
}

import java.time.Instant
def buildTime = Instant.now()

reckon {
	vcs = git(project.grgit).withTagsMatching ~/dummy-(.+)/
	
	def stage = projectProperty 'status'
	def scope = projectProperty "${project.name}.scope" or 'scope' orElse 'patch'
	
	normalVersion = basedOn(stage) {
		normally scopedUsing(scope)
		when 'development' then '0.0.0'
	}
	
	preReleaseVersion = basedOn(stage) {
		normally stageUsing(stage)
		when 'final' then none
		when 'development' then compound {
			preRelease = timestamp(buildTime)
			buildMetadata = commitId
		}
	}
}

task('printVersion') {
	doLast {
		println project.version
	}
}

'''
		
		grgit.add(patterns: ['.'])
		grgit.commit(
			author: mrBlobby,
			committer: mrBlobby,
			message: 'Initial commit'
		)
		
		grgit.tag.add(name: 'non-version-tag')
		grgit.tag.add(name: 'dummy-1.2.3')
		
		projectDir.newFile('code.txt') << '''\
Here is some really good code
'''
		grgit.add(patterns: ['.'])
		grgit.commit(
			author: mrBlobby,
			committer: mrBlobby,
			message: 'Add some code'
		)
		grgit.tag.add(name: 'dummy-1.3.0-alpha.1')
	}
	
	def cleanup() {
		grgit.close()
	}
	
	def "A test"() {
		expect:
		true
	}

	@Unroll
	def "The version is correctly determined when #desc properties are specified in Gradle #gradleVersion"(
			List<String> properties, def expected, String gradleVersion, String desc) {
		setup:
		List<String> args = ['printVersion', '--stacktrace'] + properties.collect {'-P' + it}
		println args
		if(expected instanceof String) {
			expected = Pattern.quote(expected)
		}

		when:
		def result = GradleRunner.create()
				.withPluginClasspath()
				.withProjectDir(projectDir.root)
				.withGradleVersion(gradleVersion)
				.withArguments(args)
				.build()

		then:
		result.task(':printVersion').outcome == SUCCESS
		result.output =~ expected
		
		where:
		[properties,                      expected,            gradleVersion] << [[
		[[],                              ~/0\.0\.0-\d{8}\+/],
		[['status=alpha'],                '1.2.4-alpha.1'],
		[['status=alpha', 'scope=minor'], '1.3.0-alpha.2'],
		[['status=final', 'scope=major'], '2.0.0']
		
		],                                       compatibleGradleVersions]
		.combinations().collect {it[0] + it[1]}
		
		desc = properties.empty ? 'no' : properties.join(' and ')
	}

}
