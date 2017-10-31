package org.uulib.reckon.strategy;

import java.util.Objects;

import org.ajoberstar.reckon.core.NormalStrategy;
import org.ajoberstar.reckon.core.PreReleaseStrategy;
import org.ajoberstar.reckon.core.VcsInventory;

import com.github.zafarkhaja.semver.Version;

public final class ConstantVersionStrategy implements NormalStrategy, PreReleaseStrategy {
	
	private final Version version;
	
	public ConstantVersionStrategy(Version version) {
		this.version = Objects.requireNonNull(version);
	}

	@Override
	public Version reckonTargetVersion(VcsInventory inventory, Version targetNormal) {
		return version;
	}

	@Override
	public Version reckonNormal(VcsInventory inventory) {
		return version;
	}
	
}