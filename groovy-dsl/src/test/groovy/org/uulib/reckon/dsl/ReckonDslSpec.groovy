package org.uulib.reckon.dsl

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.Callable
import org.ajoberstar.reckon.core.VcsInventory
import org.uulib.reckon.VcsInventoryBuilder

import spock.lang.*

class ReckonDslSpec extends Specification {
	
	@Shared Instant time = Instant.now()
	@Shared DateTimeFormatter format = DateTimeFormatter.ofPattern('uuuuMMdd')
			.withZone(ZoneId.of('UTC'));
	@Shared String timestamp = format.format(time)
	
	@Shared VcsInventory vcsInventory = new VcsInventoryBuilder()
					.setCommitId('hello')
					.setBaseVersion('1.0.1-beta.2')
					.setBaseNormal('1.0.0')
					.addClaimedVersions('1.0.1-alpha.1', '1.0.1-beta.1', '1.0.1-beta.2')
					.build()

	def "Versions can be reckoned with constant values"() {
		expect:
		'1.2.3-test' == Reckon.reckon {
			normalVersion = '1.2.3'
			preReleaseVersion = 'test'
			vcs = vcsInventory
		} as String
	}
	
	def "Compound versions can be reckoned with constant values"() {
		expect:
		'1.0.1-alpha.2+test' == Reckon.reckon {
			vcs = vcsInventory
			normalVersion = '1.0.1'
			preReleaseVersion {
				preRelease = 'alpha.2'
				buildMetadata = 'test'
			}
		} as String
	}
	
	def "None and commitId can be used for pre-release and build-metadata parts"() {
		expect:
		'1.2.3+hello' == Reckon.reckon {
			vcs = vcsInventory
			normalVersion = '1.2.3'
			preReleaseVersion {
				preRelease = none
				buildMetadata = commitId
			}
		} as String
	}
	
	@Unroll
	def "Scoped normal strategies can be used (#scope)"(String scope, String version) {
		expect:
		version == Reckon.reckon {
			vcs = vcsInventory
			normalVersion = scopedUsing(scope)
		} as String
		
		where:
		scope   | version
		'major' | '2.0.0'
		'minor' | '1.1.0'
		'patch' | '1.0.1'
	}
	
	@Unroll
	def "Staged part strategies can be used"(String stage, String expected) {
		"1.0.1-${expected}" == Reckon.reckon {
			vcs = vcsInventory
			normalVersion = '1.0.1'
			preReleaseVersion = stageUsing({stage})
		} as String
		
		where:
		stage   | expected
		'alpha' | 'alpha.2'
		'beta'  | 'beta.3'
		'rc'    | 'rc.1'
	}
	
	def "A timestamp can be used as the pre-release part"() {
		expect:
		"4.3.2-$timestamp" == Reckon.reckon {
			vcs = vcsInventory
			normalVersion = '4.3.2'
			preReleaseVersion = timestamp(time)
		} as String
	}
	
	def "Test of a complex reckoning"(String scope, String stage, String expected) {
		Callable<String> stageSupplier = {stage}
		when:
		ReckonedVersion v = Reckon.reckon {
			vcs = vcsInventory
			
			normalVersion = basedOn(stageSupplier) {
				normally scopedUsing({scope})
				when absent then '0.0.0'
			}
			
			preReleaseVersion = basedOn({stage}) {
				normally stageUsing({stage})
				when 'final' then none
				when absent then compound {
					preRelease = timestamp(time)
					buildMetadata = commitId
				}
			}
		}
		
		then:
		expected == v as String
		
		where:
		
		scope   | stage   | expected
		'patch' | 'alpha' | '1.0.1-alpha.2'
		'minor' | 'alpha' | '1.1.0-alpha.1'
		null    | null    | "0.0.0-${timestamp}+hello"
		'major' | 'final' | '2.0.0'
		'major' | 'rc'    | '2.0.0-rc.1'
		'patch' | 'beta'  | '1.0.1-beta.3' 
	}
	
}
