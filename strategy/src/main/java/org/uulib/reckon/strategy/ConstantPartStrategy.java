package org.uulib.reckon.strategy;

import java.util.Optional;

import org.ajoberstar.reckon.core.VcsInventory;

import com.github.zafarkhaja.semver.Version;

public final class ConstantPartStrategy implements PreReleasePartStrategy, BuildMetadataPartStrategy {
	
	public static final ConstantPartStrategy NONE = new ConstantPartStrategy(Optional.empty());
	
	private final Optional<String> part;
	
	public ConstantPartStrategy(String part) {
		this(Optional.of(part));
	}
	
	private ConstantPartStrategy(Optional<String> part) {
		this.part = part;
	}

	@Override
	public Optional<String> reckonBuildMetadata(VcsInventory inventory) {
		return part;
	}

	@Override
	public Optional<String> reckonPreRelease(VcsInventory inventory, Version normalVersion) {
		return part;
	}

}
