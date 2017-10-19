package org.uulib.reckon.strategy;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ajoberstar.reckon.core.PreReleaseStrategy;
import org.ajoberstar.reckon.core.VcsInventory;

import com.github.zafarkhaja.semver.Version;

/**
 * A stage-based {@linkplain PreReleaseStrategy} that can be built from sub-strategies for generating the
 * {@linkplain Version#getPreReleaseVersion() pre-release version} and
 * {@linkplain Version#getBuildMetadata() build metadata} separately. Optionally, separate strategies can be defined
 * for each stage.
 * 
 * This class treats all versions as having a "stage" of some kind. Each instance of this class will have a number
 * of stages defined as follows:
 * <ul>
 *   <li>A "development" stage, indicating an in-development, throw-away state of the project.
 *       When no stage is supplied during reckoning, this will be assumed by default.</li>
 *   <li>Zero or more "pre-release" stages. These typically indicate a candidate or demonstration state of the project,
 *       and would normally have their stage name included in the pre-release version information. Because of this,
 *       to ensure compatibility with the <a href="http://semver.org/">semantic version specification</a>,
 *       pre-release stages must always be in alphabetical order.</li>
 *   <li>A "final" stage, indicating a generally-consumable release. This will always have a blank pre release
 *       version, in line with the semantic version specification.
 * </ul>
 * 
 * @author hWorblehat
 */
public class CompoundStagePreReleaseStrategy implements PreReleaseStrategy {
	
	public static final String DEFAULT_DEVELOPMENT_STAGE = "";
	public static final String DEFAULT_FINAL_STAGE = "final";
	
	private final String defaultStage;
	private final PreReleasePartStrategy defaultPreReleasePartStrategy;
	private final BuildMetadataPartStrategy defaultBuildInfoPartStrategy;
	private final Map<String, PreReleaseReckoners> specificReckoners;
	private final Supplier<Optional<String>> stageSupplier;
	
	private CompoundStagePreReleaseStrategy(String defaultStage, PreReleasePartStrategy defaultPreReleasePartStrategy,
			BuildMetadataPartStrategy defaultBuildInfoPartStrategy, Map<String, PreReleaseReckoners> specificReckoners,
			Supplier<Optional<String>> stageSupplier) {
		this.defaultStage = defaultStage;
		this.defaultPreReleasePartStrategy = defaultPreReleasePartStrategy;
		this.defaultBuildInfoPartStrategy = defaultBuildInfoPartStrategy;
		this.specificReckoners = specificReckoners;
		this.stageSupplier = stageSupplier;
	}
	
	@Override
	public Version reckonTargetVersion(VcsInventory inventory, Version targetNormal) {
		return reckonTargetVersion(inventory, targetNormal, stageSupplier.get());
	}

	/**
	 * Reckons the version using the stage provided by the <b>stageOptional</b> parameter,
	 * rather than that supplied by the receiver's stage supplier.
	 * 
	 * @param inventory An inventory of version labels on the current state of the associated version control system.
	 * @param targetNormal The reckoned normal version (without pre-release information).
	 * @param stageOptional The stage to reckon with.
	 * @return The reckoned version.
	 */
	public Version reckonTargetVersion(VcsInventory inventory, Version targetNormal, Optional<String> stageOptional) {
		String stage = stageOptional.orElse(defaultStage);
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
	
	private static class PreReleaseReckoners {
		Optional<PreReleasePartStrategy> preReleasePartStrategy = Optional.empty();
		Optional<BuildMetadataPartStrategy> buildInfoPartStrategy = Optional.empty();
		
		void setPreReleasePartStrategy(PreReleasePartStrategy strategy) {
			this.preReleasePartStrategy = Optional.of(strategy);
		}
		
		void setBuildMetadataPartStrategy(BuildMetadataPartStrategy strategy) {
			this.buildInfoPartStrategy = Optional.of(strategy);
		}
	}
	
	/**
	 * @return A new {@linkplain Builder} for defining a compound strategy.
	 */
	public static Builder builder() {
		return new Builder();
	}
	
	/**
	 * A builder-style class for defining compound stage pre-release strategies.
	 * 
	 * @author hWorblehat
	 */
	public static class Builder {
		
		/**
		 * The default name of the stage indicating a 'final' release.
		 * @see CompoundStagePreReleaseStrategy#DEFAULT_FINAL_STAGE
		 */
		public static final String DEFAULT_FINAL_STAGE = CompoundStagePreReleaseStrategy.DEFAULT_FINAL_STAGE;
		
		/**
		 * The default name of the stage indicating a throw-away 'development' release.
		 * @see CompoundStagePreReleaseStrategy#DEFAULT_DEVELOPMENT_STAGE
		 */
		public static final String DEFAULT_DEVELOPMENT_STAGE =
				CompoundStagePreReleaseStrategy.DEFAULT_DEVELOPMENT_STAGE;
		
		private String developmentStage = DEFAULT_DEVELOPMENT_STAGE;
		private String finalStage = DEFAULT_FINAL_STAGE;
		
		private PreReleaseReckoners devReckoners = new PreReleaseReckoners();
		private PreReleaseReckoners finalReckoners = new PreReleaseReckoners();
		
		private PreReleasePartStrategy defaultPreReleasePartStrategy = new NumberedStagePreReleasePartStrategy();
		private BuildMetadataPartStrategy defaultBuildMetadataPartStrategy = BuildMetadataPartStrategy.NONE;
		
		private Map<String, PreReleaseReckoners> preReleaseStages = new HashMap<>(0);
		
		private Builder() {
			finalReckoners.setPreReleasePartStrategy(PreReleasePartStrategy.NONE);
		}
		
		/**
		 * Sets the name of the stage to use to indicate a 'final' release.
		 * The defult value is {@value #DEFAULT_FINAL_STAGE}.
		 * <p>
		 * This name will never appear in the version identifier, as final stages always have blank pre-release
		 * metadata; the name of the final stage is only used for identification purposes when inspecting the value
		 * supplied by the stage supplier.
		 * 
		 * @param finalStage The name to use for the final release stage.
		 * @return A reference to {@code this} builder
		 * 
		 * @see #build(Supplier)
		 */
		public Builder setFinalStage(String finalStage) {
			Objects.requireNonNull(finalStage);
			if(developmentStage.equals(finalStage) || preReleaseStages.containsKey(finalStage)) {
				stageInUse(finalStage);
			}
			this.finalStage = finalStage;
			return this;
		}
		
		/**
		 * Sets the name of the stage to use to indicate a 'development' release. This is an in-development,
		 * throw-away state of the project. The default value is {@value #DEFAULT_DEVELOPMENT_STAGE}.
		 * When no stage is supplied during reckoning, this stage will be assumed as the default.
		 * 
		 * @param developmentStage The name of the development stage.
		 * @return A reference to {@code this} builder
		 */
		public Builder setDevelopmentStage(String developmentStage) {
			Objects.requireNonNull(developmentStage);
			if(finalStage.equals(developmentStage) || preReleaseStages.containsKey(developmentStage)) {
				stageInUse(developmentStage);
			}
			this.developmentStage = developmentStage;
			return this;
		}
		
		/**
		 * Sets the list of 'pre-release' stages that are available. By default, this is empty.
		 * The list <em>must</em> be specified in alphabetical order, in line with the
		 * <a href="http://semver.org/">semantic version specification</a>.
		 * <p>
		 * If this list replaces a previously-defined list in this builder, any strategies associated with stages
		 * found in both the old and new lists will be kept.
		 * 
		 * @param stages The list of pre-release stages to use.
		 * @return A reference to {@code this} builder
		 */
		public Builder setPreReleaseStages(String... stages) {
			for(int i=1; i<stages.length; ++i) {
				if(stages[i-1].compareTo(stages[i]) > 0) {
					throw new IllegalArgumentException("Pre-release stages must be in alphabetical order.");
				}
			}
			
			Map<String, PreReleaseReckoners> preReleaseStages = Stream.of(stages).collect(Collectors.toMap(
					Function.identity(),
					s -> Optional.ofNullable(this.preReleaseStages.get(s)).orElseGet(PreReleaseReckoners::new)
			));
			
			for(String s: new String[] {developmentStage, finalStage}) {
				if(preReleaseStages.containsKey(s)) {
					stageInUse(s);
				}
			}
			
			this.preReleaseStages = preReleaseStages;
			return this;
		}
		
		private void stageInUse(String stage) {
			throw new IllegalArgumentException("The stage name '" + stage + "' is already in use.");
		}
		
		/**
		 * Sets the {@linkplain PreReleasePartStrategy} to use by default if here is no specific strategy associated
		 * with the stage being reckoned with.
		 * By default, this is an instance of {@linkplain NumberedStagePreReleasePartStrategy}.
		 * 
		 * @param strategy The default strategy to use.
		 * @return A reference to {@code this} builder
		 */
		public Builder setDefaultPreReleasePartStrategy(PreReleasePartStrategy strategy) {
			this.defaultPreReleasePartStrategy = Objects.requireNonNull(strategy);
			return this;
		}
		
		/**
		 * Sets the {@linkplain BuildMetadataPartStrategy} to use by default if here is no specific strategy associated
		 * with the stage being reckoned with.
		 * By default, a strategy that returns blank build metadata is used.
		 * 
		 * @param strategy The default strategy to use.
		 * @return A reference to {@code this} builder
		 */
		public Builder setDefaultBuildMetadataPartStrategy(BuildMetadataPartStrategy strategy) {
			this.defaultBuildMetadataPartStrategy = Objects.requireNonNull(strategy);
			return this;
		}
		
		/**
		 * Sets the {@linkplain PreReleasePartStrategy} to use when the given stage is supplied during reckoning.
		 * 
		 * @param stage The stage to associate the strategy to, which must be a previously defined development or
		 *              pre-release stage name.
		 * @param strategy The strategy to associate with the stage.
		 * @return A reference to {@code this} builder
		 * 
		 * @see #setDevelopmentPreReleasePartStrategy(PreReleasePartStrategy)
		 */
		public Builder setPreReleasePartStrategy(String stage, PreReleasePartStrategy strategy) {
			if(finalStage.equals(stage)) {
				throw new IllegalArgumentException(
						"The pre-release part strategy for the final stage '" + stage + "' cannot be changed.");
			}
			
			getReckonersFor(stage).setPreReleasePartStrategy(strategy);
			return this;
		}
		
		/**
		 * Sets the {@linkplain PreReleasePartStrategy} to use when the development stage is supplied during reckoning.
		 * 
		 * @param strategy The strategy to associate with the development stage.
		 * @return A reference to {@code this} builder
		 */
		public Builder setDevelopmentPreReleasePartStrategy(PreReleasePartStrategy strategy) {
			return setPreReleasePartStrategy(developmentStage, strategy);
		}
		
		/**
		 * Sets the {@linkplain BuildMetadataPartStrategy} to use when the given stage is supplied during reckoning.
		 * 
		 * @param stage The stage to associate the strategy to, which must be a previously defined development, final,
		 *              or pre-release stage name.
		 * @param strategy The strategy to associate with the stage.
		 * @return A reference to {@code this} builder
		 * 
		 * @see #setDevelopmentBuildMetadataStrategy(BuildMetadataPartStrategy)
		 * @see #setFinalBuildMetadataStrategy(BuildMetadataPartStrategy)
		 */
		public Builder setPreReleaseBuildMetadataStrategy(String stage, BuildMetadataPartStrategy strategy) {
			getReckonersFor(stage).setBuildMetadataPartStrategy(strategy);
			return this;
		}
		
		/**
		 * Sets the {@linkplain BuildMetadataPartStrategy} to use when the development stage is supplied during
		 * reckoning.
		 * 
		 * @param strategy The strategy to associate with the development stage.
		 * @return A reference to {@code this} builder
		 */
		public Builder setDevelopmentBuildMetadataStrategy(BuildMetadataPartStrategy strategy) {
			return setPreReleaseBuildMetadataStrategy(developmentStage, strategy);
		}
		
		/**
		 * Sets the {@linkplain BuildMetadataPartStrategy} to use when the final stage is supplied during
		 * reckoning.
		 * 
		 * @param strategy The strategy to associate with the final stage.
		 * @return A reference to {@code this} builder
		 */
		public Builder setFinalBuildMetadataStrategy(BuildMetadataPartStrategy strategy) {
			return setPreReleaseBuildMetadataStrategy(finalStage, strategy);
		}
		
		private PreReleaseReckoners getReckonersFor(String stage) {
			PreReleaseReckoners reck = preReleaseStages.get(stage);
			if(reck!=null) {
				return reck;
			} else if(developmentStage.equals(stage)) {
				return devReckoners;
			} else if(finalStage.equals(stage)) {
				return finalReckoners;
			} else {
				throw new IllegalArgumentException("The stage '" + stage + "' is not defined.");
			}
		}
		
		/**
		 * Builds the {@linkplain CompoundStagePreReleaseStrategy}.
		 * 
		 * @param stageSupplier A {@linkplain Supplier} of the stage to use when reckoning.
		 * @return The built strategy.
		 */
		public CompoundStagePreReleaseStrategy build(Supplier<Optional<String>> stageSupplier) {
			Map<String, PreReleaseReckoners> stages = new HashMap<>(preReleaseStages.size() + 2);
			stages.put(developmentStage, devReckoners);
			stages.put(finalStage, finalReckoners);
			stages.putAll(preReleaseStages);
			
			return new CompoundStagePreReleaseStrategy(developmentStage, defaultPreReleasePartStrategy,
					defaultBuildMetadataPartStrategy, stages, stageSupplier);
		}
		
		/**
		 * Builds the {@linkplain CompoundStagePreReleaseStrategy}. The built strategy will always assume the default
		 * 'development' stage when reckoning, unless it's provided explicitly to
		 * {@link CompoundStagePreReleaseStrategy#reckonTargetVersion(VcsInventory, Version, Optional)}.
		 * 
		 * @return The built strategy.
		 */
		public CompoundStagePreReleaseStrategy build() {
			return build(Optional::empty);
		}
		
	}

}
