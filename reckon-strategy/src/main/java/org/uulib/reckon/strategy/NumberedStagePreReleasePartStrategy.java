package org.uulib.reckon.strategy;

import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.ajoberstar.reckon.core.VcsInventory;
import org.ajoberstar.reckon.core.Versions;

import com.github.zafarkhaja.semver.Version;

public class NumberedStagePreReleasePartStrategy implements PreReleasePartStrategy {
	
	private static final Pattern STAGE_REGEX = Pattern.compile("^(?<name>\\w+)\\.(?<num>\\d+)");

	@Override
	public Optional<String> reckonPreRelease(VcsInventory inventory, Version normalVersion, String stage) {
		Stream<String> existingPreReleases = inventory.getClaimedVersions().stream()
				.filter(v -> normalVersion.equals(Versions.getNormal(v)))
				.map(Version::getPreReleaseVersion);
				
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
