package org.uulib.reckon.dsl;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.ajoberstar.reckon.core.strategy.ScopeNormalStrategy;
import org.ajoberstar.reckon.core.strategy.SnapshotPreReleaseStrategy;
import org.ajoberstar.reckon.core.strategy.StagePreReleaseStrategy;
import org.uulib.reckon.strategy.ConstantVersionStrategy;
import org.uulib.reckon.strategy.NoPreReleaseStrategy;
import org.uulib.util.ConstantSupplier;
import org.uulib.util.SneakySupplier;

import com.github.zafarkhaja.semver.Version;

import groovy.util.Eval;

public interface VersionStrategies {
	
	static ConstantVersionStrategy version(Version version) {
		return new ConstantVersionStrategy(version);
	}
	
	static ConstantVersionStrategy version(String version) {
		return version(Version.valueOf(version));
	}
	
	static ConstantVersionStrategy version(int majorVersion) {
		return version(Version.forIntegers(majorVersion));
	}

	static ScopeNormalStrategy scopedUsing(Supplier<Optional<String>> scopeSupplier) {
		return new ScopeNormalStrategy(scopeSupplier);
	}

	static ScopeNormalStrategy scopedUsing(String scope) {
		return scopedUsing(ConstantSupplier.nonNull(Optional.of(scope)));
	}

	static ScopeNormalStrategy scopedUsing(Callable<String> scopeSupplier) {
		return scopedUsing(new SneakySupplier<>(() -> Optional.ofNullable(scopeSupplier.call())));
	}

	static SnapshotPreReleaseStrategy snapshotUsing(Supplier<Boolean> snapshotSupplier) {
		return new SnapshotPreReleaseStrategy(snapshotSupplier);
	}

	static SnapshotPreReleaseStrategy snapshotUsing(Callable<?> snapshotSupplier) {
		return snapshotUsing(() -> (Boolean) Eval.x(new SneakySupplier<>(snapshotSupplier).get(), "x as Boolean"));
	}

	static SnapshotPreReleaseStrategy snapshotUsing(BooleanSupplier snapshotSupplier) {
		return snapshotUsing((Supplier<Boolean>) (() -> snapshotSupplier.getAsBoolean()));
	}
	
	static StagePreReleaseStrategy stageUsing(Set<String> stages, Supplier<Optional<String>> stageSupplier) {
		return new StagePreReleaseStrategy(stages, stageSupplier);
	}

	static StagePreReleaseStrategy stageUsing(Set<String> stages, Callable<String> stageSupplier) {
		return stageUsing(stages, new SneakySupplier<>(() -> Optional.ofNullable(stageSupplier.call())));
	}
	
	static StagePreReleaseStrategy stageUsing(Set<String> stages, String stage) {
		return stageUsing(stages, ConstantSupplier.nonNull(Optional.ofNullable(stage)));
	}
	
	static final NoPreReleaseStrategy none = NoPreReleaseStrategy.INSTANCE;

}
