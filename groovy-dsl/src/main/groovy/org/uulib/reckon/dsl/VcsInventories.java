package org.uulib.reckon.dsl;

import org.ajoberstar.grgit.Grgit;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;

public interface VcsInventories {
	
	static ConfigurableGitInventorySupplier git(Repository repository) {
		return new ConfigurableGitInventorySupplier(repository);
	}
	
	static ConfigurableGitInventorySupplier git(Git git) {
		return git(git.getRepository());
	}
	
	static ConfigurableGitInventorySupplier git(Grgit grgit) {
		return git(grgit.getRepository().getJgit());
	}

}
