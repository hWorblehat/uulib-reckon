package org.uulib.reckon.strategy;

import org.ajoberstar.reckon.core.PreReleaseStrategy;
import org.ajoberstar.reckon.core.VcsInventory;

import com.github.zafarkhaja.semver.Version;

public final class NoPreReleaseStrategy implements PreReleaseStrategy {
	
	public static final NoPreReleaseStrategy INSTANCE = new NoPreReleaseStrategy();
	
	private NoPreReleaseStrategy() {}

	@Override
	public Version reckonTargetVersion(VcsInventory inventory, Version targetNormal) {
		return targetNormal;
	}

}
