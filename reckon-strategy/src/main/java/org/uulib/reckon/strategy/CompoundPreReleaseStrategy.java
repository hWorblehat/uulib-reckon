package org.uulib.reckon.strategy;

import java.util.Objects;
import java.util.function.Supplier;

import org.ajoberstar.reckon.core.PreReleaseStrategy;
import org.ajoberstar.reckon.core.VcsInventory;
import org.uulib.util.ConstantSupplier;

import com.github.zafarkhaja.semver.Version;

/**
 * A {@linkplain PreReleaseStrategy} that separates the reckoning of the version's
 * {@linkplain Version#getPreReleaseVersion() pre-release version part} and its
 * {@linkplain Version#getBuildMetadata() build metadata}. This is achieved by delegating to
 * a {@linkplain PreReleasePartStrategy} and {@linkplain BuildMetadataPartStrategy} respectively.
 * 
 * @author hWorblehat
 */
public class CompoundPreReleaseStrategy implements PreReleaseStrategy {
	
	private final Supplier<PreReleasePartStrategy> preReleasePart;
	private final Supplier<BuildMetadataPartStrategy> buildMetadataPart;
	
	/**
	 * Creates a new CompoundPreReleaseStrategy.
	 * @param preReleasePart A supplier of the {@linkplain PreReleasePartStrategy} to use.
	 * @param buildMetadataPart A supplier of the {@linkplain BuildMetadataPartStrategy} to use.
	 * 
	 * @see #builder()
	 */
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
	
	/**
	 * A builder of instances of {@linkplain CompoundPreReleaseStrategy}.
	 * 
	 * @author hWorblehat
	 */
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
