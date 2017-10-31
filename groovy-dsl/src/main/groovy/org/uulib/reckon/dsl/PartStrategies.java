package org.uulib.reckon.dsl;

import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import org.uulib.reckon.strategy.BuildMetadataPartStrategy;
import org.uulib.reckon.strategy.ConstantPartStrategy;
import org.uulib.reckon.strategy.DatePreReleasePartStrategy;
import org.uulib.reckon.strategy.NumberedStagePreReleasePartStrategy;
import org.uulib.util.ConstantSupplier;
import org.uulib.util.SneakySupplier;

public interface PartStrategies {

	static ConstantPartStrategy part(String part) {
		return new ConstantPartStrategy(part);
	}
	
	static ConstantPartStrategy buildMetadata(String part) {
		return part(part);
	}

	static ConstantPartStrategy preRelease(String part) {
		return part(part);
	}

	ConstantPartStrategy none = ConstantPartStrategy.NONE;
	
	static DatePreReleasePartStrategy timestamp(Supplier<Instant> timestampSupplier) {
		return new DatePreReleasePartStrategy(timestampSupplier);
	}
	
	static DatePreReleasePartStrategy timestamp(Callable<Instant> timestampSupplier) {
		return timestamp(new SneakySupplier<>(timestampSupplier));
	}
	
	static DatePreReleasePartStrategy timestamp(Instant timestamp) {
		return timestamp((Supplier<Instant>) ConstantSupplier.nonNull(timestamp));
	}
	
	static NumberedStagePreReleasePartStrategy stageUsing(Supplier<String> stageSupplier) {
		return NumberedStagePreReleasePartStrategy.forStage(stageSupplier);
	}
	
	static NumberedStagePreReleasePartStrategy stageUsing(Callable<String> stageSupplier) {
		return stageUsing(new SneakySupplier<>(stageSupplier));
	}
	
	static NumberedStagePreReleasePartStrategy stageUsing(String stage) {
		return stageUsing((Supplier<String>) ConstantSupplier.nonNull(stage));
	}
	
	BuildMetadataPartStrategy commitId = BuildMetadataPartStrategy.COMMIT_ID;

}
