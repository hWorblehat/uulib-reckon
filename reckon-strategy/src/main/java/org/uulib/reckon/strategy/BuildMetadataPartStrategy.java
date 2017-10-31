package org.uulib.reckon.strategy;

import java.util.Optional;

import org.ajoberstar.reckon.core.VcsInventory;

/**
 * Classes that implement this interface are used to determine the build metadata part of versions reckoned
 * using {@linkplain CompoundPreReleaseStrategy}.
 * 
 * @author hWorblehat
 */
@FunctionalInterface
public interface BuildMetadataPartStrategy {
	
	/**
	 * A {@linkplain BuildMetadataPartStrategy} that always reckons there should be no build metadata.
	 */
	public static final BuildMetadataPartStrategy NONE = ConstantPartStrategy.NONE;
	
	/**
	 * A {@linkplain BuildMetadataPartStrategy} that uses the current commit ID from the VCS inventory as the
	 * build metadata.
	 */
	public static final BuildMetadataPartStrategy COMMIT_ID = i -> Optional.of(i.getCommitId());
	
	/**
	 * Reckons the build metadata to use from the given version control system information.
	 * 
	 * @param inventory An inventory of the current version tags on the version control system.
	 * @return The reckoned build metadata.
	 */
	Optional<String> reckonBuildMetadata(VcsInventory inventory);

}
