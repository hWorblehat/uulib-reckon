package org.uulib.reckon.strategy;

import java.util.Optional;

import org.ajoberstar.reckon.core.VcsInventory;

@FunctionalInterface
public interface BuildMetadataPartStrategy {
	
	public static final BuildMetadataPartStrategy NONE = i -> Optional.empty();
	public static final BuildMetadataPartStrategy COMMIT_ID = i -> Optional.of(i.getCommitId());
	
	Optional<String> reckonBuildMetadata(VcsInventory inventory);

}
