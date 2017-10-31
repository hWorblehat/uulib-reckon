package org.uulib.reckon.dsl;

import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ajoberstar.reckon.core.VcsInventory;
import org.ajoberstar.reckon.core.VcsInventorySupplier;
import org.ajoberstar.reckon.core.git.GitInventorySupplier;
import org.eclipse.jgit.lib.Repository;

import groovy.lang.Closure;

public class ConfigurableGitInventorySupplier implements VcsInventorySupplier {
	
	private final Repository repository;
	private Function<String, Optional<String>> tagSelector = (tag) -> Optional.of(tag.replaceAll("^v", ""));
	
	public ConfigurableGitInventorySupplier(Repository repository) {
		this.repository = repository;
	}
	
	public ConfigurableGitInventorySupplier withTagsMatching(Function<String, Optional<String>> tagSelector) {
		this.tagSelector = tagSelector;
		return this;
	}
	
	public ConfigurableGitInventorySupplier withTagsMatching(Closure<String> tagSelector) {
		return withTagsMatching(tag -> Optional.ofNullable(tagSelector.call(tag)));
	}
	
	public ConfigurableGitInventorySupplier withTagsMatching(Pattern regex) {
		return withTagsMatching(tag -> {
			Matcher m = regex.matcher(tag);
			if(!m.matches()) {
				return Optional.empty();
			}
			if(m.groupCount()==1) {
				return Optional.ofNullable(m.group(1));
			}
			try {
				return Optional.ofNullable(m.group("version"));
			} catch (IllegalArgumentException e) {
				return Optional.ofNullable(m.group());
			}
		});
	}
	
	public ConfigurableGitInventorySupplier withTagsMatching(String regex) {
		return withTagsMatching(Pattern.compile(regex));
	}

	@Override
	public VcsInventory getInventory() {
		return new GitInventorySupplier(repository, tagSelector).getInventory();
	}

}
