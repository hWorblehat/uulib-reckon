package org.uulib.reckon.strategy;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.ajoberstar.reckon.core.PreReleaseStrategy;
import org.ajoberstar.reckon.core.VcsInventory;

import com.github.zafarkhaja.semver.Version;

public class CompoundStagePreReleaseStrategy implements PreReleaseStrategy {
	
	private final String defaultStage;
	private final PreReleasePartStrategy defaultPreReleasePartStrategy;
	private final BuildInfoPartStrategy defaultBuildInfoPartStrategy;
	private final Map<String, PreReleaseReckoners> specificReckoners;
	private final Supplier<Optional<String>> stageSupplier;
	
	private CompoundStagePreReleaseStrategy(String defaultStage, PreReleasePartStrategy defaultPreReleasePartStrategy,
			BuildInfoPartStrategy defaultBuildInfoPartStrategy, Map<String, PreReleaseReckoners> specificReckoners,
			Supplier<Optional<String>> stageSupplier) {
		this.defaultStage = defaultStage;
		this.defaultPreReleasePartStrategy = defaultPreReleasePartStrategy;
		this.defaultBuildInfoPartStrategy = defaultBuildInfoPartStrategy;
		this.specificReckoners = specificReckoners;
		this.stageSupplier = stageSupplier;
	}

	@Override
	public Version reckonTargetVersion(VcsInventory inventory, Version targetNormal) {
		
		String stage = stageSupplier.get().orElse(defaultStage);
		PreReleaseReckoners specificStrategies = specificReckoners.get(stage);
		if(specificStrategies==null) {
			throw new IllegalArgumentException(
					String.format("Stage '%s' is not one of %s", stage, specificReckoners.keySet()));
		}
		
		Version withPreRelease = specificStrategies.preReleasePartStrategy
				.orElse(defaultPreReleasePartStrategy)
				.reckonPreRelease(inventory, targetNormal, stage)
				.map(targetNormal::setPreReleaseVersion)
				.orElse(targetNormal);
				
		Version withBuildMetadata = specificStrategies.buildInfoPartStrategy
				.orElse(defaultBuildInfoPartStrategy)
				.reckonBuildMetadata(inventory)
				.map(withPreRelease::setBuildMetadata)
				.orElse(withPreRelease);
		
		return withBuildMetadata;
	}
	
	public static class PreReleaseReckoners {
		private Optional<PreReleasePartStrategy> preReleasePartStrategy = Optional.empty();
		private Optional<BuildInfoPartStrategy> buildInfoPartStrategy = Optional.empty();
		
		public void setPreReleasePartStrategy(PreReleasePartStrategy strategy) {
			this.preReleasePartStrategy = Optional.of(strategy);
		}
		
		public void setBuildInfoPartStrategy(BuildInfoPartStrategy strategy) {
			this.buildInfoPartStrategy = Optional.of(strategy);
		}
	}

}
