package org.uulib.reckon.dsl

import java.util.concurrent.Callable
import java.util.function.Supplier

import groovy.transform.PackageScope

@PackageScope final class Util {
	
	static def extract(def val) {
		switch(val) {
			case Supplier: return extract(val.get())
			case Callable: return extract(val.call())
			default: return val
		}
	}

}
