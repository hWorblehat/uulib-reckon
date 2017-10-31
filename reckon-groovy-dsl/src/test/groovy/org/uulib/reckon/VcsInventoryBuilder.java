package org.uulib.reckon;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.ajoberstar.reckon.core.VcsInventory;
import org.ajoberstar.reckon.core.Versions;

import com.github.zafarkhaja.semver.Version;

public class VcsInventoryBuilder {
	
	private String commitId;
	private Version currentVersion, baseVersion, baseNormal;
	private int commitsSinceBase;
	private final Set<Version> parallelNormals = new HashSet<>(), claimedVersions = new HashSet<>();
	
	public VcsInventoryBuilder() {}
	
	public VcsInventoryBuilder setCommitId(String commitId) {
		this.commitId = commitId;
		return this;
	}
	
	public VcsInventoryBuilder setCurrentVersion(Version currentVersion) {
		this.currentVersion = currentVersion;
		return this;
	}
	
	public VcsInventoryBuilder setCurrentVersion(String currentVersion) {
		return setCurrentVersion(Version.valueOf(currentVersion));
	}
	
	public VcsInventoryBuilder setBaseVersion(Version baseVersion) {
		this.baseVersion = baseVersion;
		return this;
	}
	
	public VcsInventoryBuilder setBaseVersion(String baseVersion) {
		return setBaseVersion(Version.valueOf(baseVersion));
	}
	
	public VcsInventoryBuilder setBaseNormal(Version baseNormal) {
		assert Versions.isNormal(baseNormal);
		this.baseNormal = baseNormal;
		return this;
	}
	
	public VcsInventoryBuilder setBaseNormal(String baseNormal) {
		return setBaseNormal(Version.valueOf(baseNormal));
	}
	
	public VcsInventoryBuilder setCommitsSinceBase(int commitsSinceBase) {
		assert commitsSinceBase >= 0;
		this.commitsSinceBase = commitsSinceBase;
		return this;
	}
	
	public VcsInventoryBuilder addParallelNormals(Stream<Version> parallelNormals) {
		parallelNormals.forEach(this.parallelNormals::add);
		return this;
	}
	
	public VcsInventoryBuilder addParallelNormals(Collection<Version> parallelNormals) {
		return addParallelNormals(parallelNormals.stream());
	}
	
	public VcsInventoryBuilder addParallelNormals(Version... parallelNormals) {
		return addParallelNormals(Stream.of(parallelNormals));
	}
	
	public VcsInventoryBuilder addParallelNormals(String... parallelNormals) {
		return addParallelNormals(Stream.of(parallelNormals).map(Version::valueOf));
	}
	
	public VcsInventoryBuilder addClaimedVersions(Stream<Version> claimedVersions) {
		claimedVersions.forEach(this.claimedVersions::add);
		return this;
	}
	
	public VcsInventoryBuilder addClaimedVersions(Collection<Version> claimedVersions) {
		return addClaimedVersions(claimedVersions.stream());
	}
	
	public VcsInventoryBuilder addClaimedVersions(Version... claimedVersions) {
		return addClaimedVersions(Stream.of(claimedVersions));
	}
	
	public VcsInventoryBuilder addClaimedVersions(String... claimedVersions) {
		return addClaimedVersions(Stream.of(claimedVersions).map(Version::valueOf));
	}
	
	public VcsInventory build() {
		Stream.of(currentVersion, baseVersion, baseNormal)
				.filter(v -> v!=null)
				.forEach(claimedVersions::add);
		return new VcsInventory(commitId, currentVersion, baseVersion, baseNormal, commitsSinceBase, parallelNormals,
				claimedVersions);
	}

}
