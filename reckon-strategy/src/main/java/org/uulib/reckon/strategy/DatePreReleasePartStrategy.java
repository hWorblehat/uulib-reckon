package org.uulib.reckon.strategy;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.ajoberstar.reckon.core.VcsInventory;
import org.ajoberstar.reckon.core.Versions;

import com.github.zafarkhaja.semver.Version;

public class DatePreReleasePartStrategy implements PreReleasePartStrategy {
	
	private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
	private static final DateTimeFormatter HOUR_MINUTE_SECOND = DateTimeFormatter.ofPattern("HHmmss");
	private static final DateTimeFormatter MILLISECOND = DateTimeFormatter.ofPattern("SSS");
	
	private final Supplier<Instant> timestampSupplier;
	private final DateTimeFormatter[] formats;
	
	DatePreReleasePartStrategy(Supplier<Instant> timestampSupplier, Locale formatLocale) {
		this.timestampSupplier = timestampSupplier;
		this.formats = new DateTimeFormatter[] {
			DATE.withLocale(formatLocale),
			HOUR_MINUTE_SECOND.withLocale(formatLocale),
			MILLISECOND.withLocale(formatLocale)
		};
	}

	@Override
	public Optional<String> reckonPreRelease(VcsInventory inventory, Version normalVersion, String stage) {
		Set<String> claimedPreReleases = inventory.getClaimedVersions().stream()
				.filter(v -> normalVersion.equals(Versions.getNormal(v)))
				.map(Version::getPreReleaseVersion)
				.collect(Collectors.toSet());
		
		Instant timestamp = timestampSupplier.get();
		StringBuilder rc = new StringBuilder();
		for(DateTimeFormatter format : formats) {
			rc.append(format.format(timestamp));
			if(claimedPreReleases.contains(rc.toString())) {
				rc.append('.');
			} else {
				break;
			}
		}
		return Optional.of(rc.toString());
	}

}
