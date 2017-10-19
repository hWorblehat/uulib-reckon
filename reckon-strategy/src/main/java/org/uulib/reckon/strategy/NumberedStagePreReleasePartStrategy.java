package org.uulib.reckon.strategy;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.ajoberstar.reckon.core.VcsInventory;
import org.ajoberstar.reckon.core.Versions;

import com.github.zafarkhaja.semver.Version;

/**
 * A {@linkplain PreReleasePartStrategy} that formats pre-release version parts as {@code <stage>.<number>}, where
 * the number is incremented for each new version using the same normal version and stage.
 * 
 * @author hWorblehat
 */
public class NumberedStagePreReleasePartStrategy implements PreReleasePartStrategy {
	
	private static final Pattern STAGE_REGEX = Pattern.compile("^(?<name>\\w+)\\.(?<num>\\d+)");
	
	private final Supplier<String> stageSupplier;
	
	public static NumberedStagePreReleasePartStrategy forStageOrBlank(Supplier<Optional<String>> stageSupplier) {
		return forStageOrDefault(stageSupplier, "");
	}
	
	public static NumberedStagePreReleasePartStrategy forStageOrDefault(
			Supplier<Optional<String>> stageSupplier, String defaultStage) {
		Objects.requireNonNull(stageSupplier);
		Objects.requireNonNull(defaultStage);
		
		return new NumberedStagePreReleasePartStrategy(() -> stageSupplier.get().orElse(defaultStage));
	}
	
	public static NumberedStagePreReleasePartStrategy forStage(String stage) {
		return new NumberedStagePreReleasePartStrategy(() -> stage);
	}
	
	public static NumberedStagePreReleasePartStrategy forStage(Supplier<String> stageSupplier) {
		return new NumberedStagePreReleasePartStrategy(Objects.requireNonNull(stageSupplier));
	}
	
	protected NumberedStagePreReleasePartStrategy(Supplier<String> stageSupplier) {
		this.stageSupplier = stageSupplier;
	}

	@Override
	public Optional<String> reckonPreRelease(VcsInventory inventory, Version normalVersion) {
		Stream<String> existingPreReleases = inventory.getClaimedVersions().stream()
				.filter(v -> normalVersion.equals(Versions.getNormal(v)))
				.map(Version::getPreReleaseVersion);
		
		String stage = stageSupplier.get();
		return Optional.of(stage.isEmpty()
				? reckonWithBlankStage(existingPreReleases)
				: reckonWithStage(existingPreReleases, stage));
	}
	
	private String reckonWithBlankStage(Stream<String> existingPreReleases) {
		int previousStageNum = existingPreReleases
				.filter(s -> s.matches("\\d+"))
				.mapToInt(Integer::parseInt)
				.max()
				.orElse(0);
		
		return Integer.toString(previousStageNum + 1);
	}
	
	private String reckonWithStage(Stream<String> existingPreReleases, String stage) {
		int previousStageNum = existingPreReleases
				.map(STAGE_REGEX::matcher)
				.filter(m -> m.matches() && m.group("name").equals(stage))
				.mapToInt(m -> Integer.parseInt(m.group("num")))
				.max()
				.orElse(0);
		
		return new StringBuilder(stage).append('.').append(previousStageNum + 1).toString();
	}

}
