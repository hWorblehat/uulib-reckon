package org.uulib.reckon.strategy;

import java.util.Optional;

import org.ajoberstar.reckon.core.VcsInventory;

import com.github.zafarkhaja.semver.Version;

/**
 * Classes that implement this interface are used to determine the pre-release part of the version reckoned by
 * instances of {@linkplain CompoundPreReleaseStrategy}.
 * 
 * @author hWorblehat
 */
@FunctionalInterface
public interface PreReleasePartStrategy {
	
	/**
	 * A {@linkplain PreReleasePartStrategy} that always reckons there should be no pre-release information.
	 */
	public static final PreReleasePartStrategy NONE = (i,v) -> Optional.empty();
	
	/**
	 * Determines the pre-release information to append to the given normal version.
	 * 
	 * @param inventory An inventory of the current version tags on the version control system.
	 * @param normalVersion The normal version the pre-release information will be appended to.
	 * @return The pre-release information to use.
	 */
	Optional<String> reckonPreRelease(VcsInventory inventory, Version normalVersion);

}
