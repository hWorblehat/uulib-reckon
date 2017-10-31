package org.uulib.reckon.dsl;

import java.util.concurrent.Callable;

import org.uulib.util.ExceptionallyLazy;
import com.github.zafarkhaja.semver.Version;

public class ReckonedVersion {
	
	private final ExceptionallyLazy<Version, ?> version;
	
	public ReckonedVersion(Callable<Version> reckoner) {
		this.version = ExceptionallyLazy.ofCallable(reckoner);
	}
	
	public String asString() throws Exception {
		return asVersion().toString();
	}
	
	public Version asVersion() throws Exception {
		return version.get();
	}
	
	public <T> T asType(Class<T> type) throws Exception {
		if(type.isAssignableFrom(String.class)) {
			return type.cast(asString());
		} else if(type.isAssignableFrom(Version.class)) {
			return type.cast(asVersion());
		} else {
			throw new ClassCastException("ReckonedVersion cannot be coerced into " + type);
		}
	}
	
	@Override
	public String toString() {
		try {
			return asString();
		} catch (Exception e) {
			throw (RuntimeException) e;
		}
	}

}
