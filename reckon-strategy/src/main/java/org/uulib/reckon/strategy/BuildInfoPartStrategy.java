package org.uulib.reckon.strategy;

import java.util.Optional;

import org.ajoberstar.reckon.core.VcsInventory;

@FunctionalInterface
public interface BuildInfoPartStrategy {
	
	public static final BuildInfoPartStrategy NONE = i -> Optional.empty();
	public static final BuildInfoPartStrategy COMMIT_ID = i -> Optional.of(i.getCommitId());
	
	Optional<String> reckonBuildMetadata(VcsInventory inventory);

}
