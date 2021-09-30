package de.empri.devops.gitprivacy.preferences;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.egit.core.internal.IRepositoryCommit;
import org.eclipse.egit.ui.internal.repository.tree.RepositoryNode;
import org.eclipse.jgit.lib.Repository;

public class HasBeenInitializedPropertyTester extends PropertyTester {

	public static final List<String> HOOKS = Arrays.asList("pre-commit", "pre-push", "post-commit", "post-rewrite");

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (!((receiver instanceof RepositoryNode) || (receiver instanceof IRepositoryCommit))) {
			return false;
		}

		Repository repository = null;
		if (receiver instanceof IRepositoryCommit) {
			repository = ((IRepositoryCommit) receiver).getRepository();
		} else if (receiver instanceof RepositoryNode) {
			RepositoryNode repositoryNode = (RepositoryNode) receiver;
			repository = repositoryNode.getRepository();
		}

		Path repoDirectory = repository.getDirectory().toPath();
		Path hooksDirectory = repoDirectory.resolve("hooks");
		List<Path> missing = new ArrayList<>(HOOKS.size());
		for (String hookName : HOOKS) {
			Path hook = hooksDirectory.resolve(hookName);
			if (isGitPrivacyMissing(hooksDirectory, hook)) {
				missing.add(hook);
			}
		}

		return missing.isEmpty();
	}

	private boolean isGitPrivacyMissing(Path hooksDirectory, Path hook) {
		if (!hook.toFile().exists()) {
			return true;
		}
		try (Stream<String> lines = Files.lines(hook)) {
			if (lines.anyMatch(line -> line.contains("git-privacy"))) {
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return true;
		}
		return false;
	}

}
