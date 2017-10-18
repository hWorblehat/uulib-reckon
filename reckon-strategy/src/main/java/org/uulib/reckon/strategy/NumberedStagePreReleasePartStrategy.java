package org.uulib.reckon.strategy;

import java.util.Optional;
import java.util.regex.Pattern;

import org.ajoberstar.reckon.core.VcsInventory;
import org.ajoberstar.reckon.core.Versions;

import com.github.zafarkhaja.semver.Version;

public class NumberedStagePreReleasePartStrategy implements PreReleasePartStrategy {
	
	private static final Pattern STAGE_REGEX = Pattern.compile("^(?<name>\\w+)\\.(?<num>\\d+)");

	@Override
	public Optional<String> reckonPreRelease(VcsInventory inventory, Version normalVersion, String stage) {
		int previousStageNum = inventory.getClaimedVersions().stream()
				.filter(v -> normalVersion.equals(Versions.getNormal(v)))
				.map(v -> STAGE_REGEX.matcher(v.getPreReleaseVersion()))
				.filter(m -> m.matches() && m.group("name").equals(stage))
				.mapToInt(m -> Integer.parseInt(m.group("num")))
				.max()
				.orElse(0);
				
		return Optional.of(new StringBuilder(stage).append('.').append(previousStageNum + 1).toString());
	}

}
