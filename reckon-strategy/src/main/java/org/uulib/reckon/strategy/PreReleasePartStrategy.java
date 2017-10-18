package org.uulib.reckon.strategy;

import java.util.Optional;

import org.ajoberstar.reckon.core.VcsInventory;

import com.github.zafarkhaja.semver.Version;

@FunctionalInterface
public interface PreReleasePartStrategy {
	
	public static final PreReleasePartStrategy NONE = (i,v,s) -> Optional.empty();
	
	Optional<String> reckonPreRelease(VcsInventory inventory, Version normalVersion, String stage);

}
