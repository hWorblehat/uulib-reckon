package org.uulib.dsl

import java.util.concurrent.Callable

import org.uulib.dsl.basedon.BasedOn
import org.uulib.dsl.basedon.BasedOnSpec

import spock.lang.*

class BasedOnDslSpec extends Specification {
	
	Callable<String> species = Mock(Callable)
	
	Callable<String> val = BasedOn.basedOn(test) {
		normally "hi"
		when absent then '?'
		when 'elephant' then 'trump'
		when 'cat' then basedOn(test2) {
			when 'small' then {
				'meow'
			}
			when 'big' then 'roar'
		}
	}
	
	def "the default value is returned for an unrecognised input"() {
		expect:
		'hi'==BasedOn.basedOn('person') {
			normally 'hi'
			when 'elephant' then 'trump'
		}
	}
	
	def "the default value is retuned for a null/empty input"() {
		expect:
		'hi'==BasedOn.basedOn(Optional.empty()) {
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
