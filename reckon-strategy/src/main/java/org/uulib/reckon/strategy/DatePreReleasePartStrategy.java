package org.uulib.reckon.strategy;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.Locale.Category;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.ajoberstar.reckon.core.VcsInventory;
import org.ajoberstar.reckon.core.Versions;

import com.github.zafarkhaja.semver.Version;

/**
 * A {@linkplain PreReleasePartStrategy} that reckons a unique timestamp as the pre-release part of the version.
 * <p>
 * The reckoning follows an algorithm similar to the following:
 * <ul>
 *   <li>A numeric timestamp of the format {@code <year><month><day-of-month>} is used as the initial candidate.</li>
 *   <li>If no similar version exists in the inventory with the same stamp, then reckoning is done. Otherwise,
 *       a numeric timestamp of the format {@code <hour><minute><second>} is appended after a dot to the initial
 *       candidate.</li>
 *   <li>If no similar version exists in the inventory with the same stamp, then reckoning is done. Otherwise,
 *       a numeric timestamp of the format {@code <milliseconds-in-second>} is appended after a dot to the previous
 *       candidate.</li>
 * </ul>
 * The time instant used to format all of the above timestamps will be identical. It is defined by the
 * {@linkplain Supplier} passed to this class's constructor.
 * 
 * @author hWorblehat
 */
public class DatePreReleasePartStrategy implements PreReleasePartStrategy {
	
	private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
	private static final DateTimeFormatter HOUR_MINUTE_SECOND = DateTimeFormatter.ofPattern("HHmmss");
	private static final DateTimeFormatter MILLISECOND = DateTimeFormatter.ofPattern("SSS");
	
	private final Supplier<Instant> timestampSupplier;
	private final DateTimeFormatter[] formats;
	
	/**
	 * Creates a new DatePreReleasePartStrategy that uses the default locale for formatting timestamps.
	 * @param timestampSupplier The supplier to obtain it's timestamp for during reckoning. This will be invoked
	 *                          once per call to {@link #reckonPreRelease(VcsInventory, Version, String)}.
	 */
	public DatePreReleasePartStrategy(Supplier<Instant> timestampSupplier) {
		this(timestampSupplier, Locale.getDefault(Category.FORMAT));
	}
	
	/**
	 * Creates a new DatePreReleasePartStrategy.
	 * @param timestampSupplier The supplier to obtain it's timestamp for during reckoning. This will be invoked
	 *                          once per call to {@link #reckonPreRelease(VcsInventory, Version, String)}.
	 * @param formatLocale The locale to use to form the timestamps.
	 */
	public DatePreReleasePartStrategy(Supplier<Instant> timestampSupplier, Locale formatLocale) {
		this.timestampSupplier = timestampSupplier;
		this.formats = new DateTimeFormatter[] {
			DATE.withLocale(formatLocale),
			HOUR_MINUTE_SECOND.withLocale(formatLocale),
			MILLISECOND.withLocale(formatLocale)
		};
	}

	@Override
	public Optional<String> reckonPreRelease(VcsInventory inventory, Version normalVersion) {
		Set<String> claimedPreReleases = inventory.getClaimedVersions().stream()
				.filter(v -> normalVersion.equals(Versions.getNormal(v)))
				.map(Version::getPreReleaseVersion)
				.collect(Collectors.toSet());
		
		Instant timestamp = timestampSupplier.get();
		StringBuilder sb = new StringBuilder();
		for(DateTimeFormatter format : formats) {
			sb.append(format.format(timestamp));
			if(claimedPreReleases.contains(sb.toString())) {
				sb.append('.');
			} else {
				break;
			}
		}
		String rc = sb.toString();
		if(rc.endsWith(".")) {
			throw new IllegalStateException("A similar version with an identical timestamp (to millisecond precision) already exists in the VCS inventory.");
		}
		return Optional.of(rc);
	}

}
