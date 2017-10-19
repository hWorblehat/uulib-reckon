package org.uulib.reckon.strategy;

import java.util.Objects;
import java.util.function.Supplier;

import org.ajoberstar.reckon.core.PreReleaseStrategy;
import org.ajoberstar.reckon.core.VcsInventory;
import org.uulib.reckon.util.ConstantSupplier;

import com.github.zafarkhaja.semver.Version;

public class CompoundPreReleaseStrategy implements PreReleaseStrategy {
	
	private final Supplier<PreReleasePartStrategy> preReleasePart;
	private final Supplier<BuildMetadataPartStrategy> buildMetadataPart;
	
	public CompoundPreReleaseStrategy(Supplier<PreReleasePartStrategy> preReleasePart,
			Supplier<BuildMetadataPartStrategy> buildMetadataPart) {
		this.preReleasePart = Objects.requireNonNull(preReleasePart);
		this.buildMetadataPart = Objects.requireNonNull(buildMetadataPart);
	}

	@Override
	public Version reckonTargetVersion(VcsInventory inventory, Version targetNormal) {
		
		Version withPreReleasePart = preReleasePart.get()
				.reckonPreRelease(inventory, targetNormal)
				.map(targetNormal::setPreReleaseVersion)
				.orElse(targetNormal);
		
		Version withBuildMetadataPart = buildMetadataPart.get()
				.reckonBuildMetadata(inventory)
				.map(withPreReleasePart::setBuildMetadata)
				.orElse(withPreReleasePart);
		
		return withBuildMetadataPart;
	}
	
	public static Builder builder() {
		return builder(PreReleasePartStrategy.NONE);
	}
	
	public static Builder builder(PreReleasePartStrategy defaultPreReleasePart) {
		return new Builder(ConstantSupplier.nonNull(defaultPreReleasePart));
	}
	
	public static Builder builder(Supplier<PreReleasePartStrategy> preReleasePart) {
		return new Builder(Objects.requireNonNull(preReleasePart));
	}
	
	public static class Builder {
		
		private Supplier<PreReleasePartStrategy> preReleasePart;
		private Supplier<BuildMetadataPartStrategy> buildMetadataPart = () -> BuildMetadataPartStrategy.NONE;
		
		private Builder(Supplier<PreReleasePartStrategy> preReleasePart) {
			this.preReleasePart = preReleasePart;
		}
		
		public Builder setPreReleasePart(Supplier<PreReleasePartStrategy> preReleasePart) {
			this.preReleasePart = Objects.requireNonNull(preReleasePart);
			return this;
		}
		
		public Builder setPreReleasePart(PreReleasePartStrategy preReleasePart) {
			return setPreReleasePart(ConstantSupplier.nonNull(preReleasePart));
		}
		
		public Builder setBuildMetadataPart(Supplier<BuildMetadataPartStrategy> buildMetadataPart) {
			this.buildMetadataPart = Objects.requireNonNull(buildMetadataPart);
			return this;
		}
		
		public Builder setBuildMetadataPart(BuildMetadataPartStrategy buildMetadataPart) {
			return setBuildMetadataPart(ConstantSupplier.nonNull(buildMetadataPart));
		}
		
		public CompoundPreReleaseStrategy build() {
			return new CompoundPreReleaseStrategy(preReleasePart, buildMetadataPart);
		}
		
	}

}
