package org.uulib.reckon;

import org.ajoberstar.reckon.core.NormalStrategy;
import org.ajoberstar.reckon.core.PreReleaseStrategy;
import org.ajoberstar.reckon.core.VcsInventory;
import org.ajoberstar.reckon.core.VcsInventorySupplier;

import com.github.zafarkhaja.semver.Version;

public final class Reckoner {

	private Reckoner() {}
	
	public static Version reckon(VcsInventorySupplier vcsInventorySupplier, NormalStrategy normalStrategy,
			PreReleaseStrategy preReleaseStrategy) {
		return reckon(vcsInventorySupplier.getInventory(), normalStrategy, preReleaseStrategy);
	}

	public static Version reckon(VcsInventory vcsInventory, NormalStrategy normalStrategy,
			PreReleaseStrategy preReleaseStrategy) {
		Version normal = normalStrategy.reckonNormal(vcsInventory);
		Version rc = preReleaseStrategy.reckonTargetVersion(vcsInventory, normal);

		if(vcsInventory.getClaimedVersions().contains(rc)
				&& !vcsInventory.getCurrentVersion().filter(c -> !rc.equals(c)).isPresent()) {
			throw new IllegalStateException(
					"Reckoned version " + rc + " has already been released.");
		}
		
		return rc;
	}

}
