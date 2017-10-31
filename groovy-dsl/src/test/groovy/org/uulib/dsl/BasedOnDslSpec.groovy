package org.uulib.dsl

import org.uulib.dsl.basedon.BasedOn

import spock.lang.*

class BasedOnDslSpec extends Specification {
	
	def "the default value is returned for an unrecognised input"() {
		expect:
		'hi'==BasedOn.basedOn('person') {
			normally 'hi'
			when 'elephant' then 'trump'
		}
	}
	
	def "the default value is retuned for a null/empty input"() {
		expect:
		'wooo!'==BasedOn.basedOn(Optional.empty()) {
			normally 'hi'
			when absent then 'wooo!'
			when 'elephant' then 'trump'
		}
	}
	
	def "the mapped value is returned for a recognised input"() {
		when:
		String speech = BasedOn.basedOn('elephant') {
			normally 'hi'
			when 'elephant' then 'trump'
		}
		
		then:
		speech=='trump'
	}
	
	

}
