package de.empri.devops.gitprivacy.preferences;

import java.io.IOException;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.egit.core.internal.IRepositoryCommit;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

public class HasFollowingCommitsPropertyTester extends PropertyTester {

	private ILog logger;

	public HasFollowingCommitsPropertyTester() {
		logger = Platform.getLog(getClass());
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (!(receiver instanceof IRepositoryCommit)) {
			return false;
		}

		IRepositoryCommit commit = (IRepositoryCommit) receiver;
		ObjectId objectId = commit.getObjectId();
		String hexshaCommit = objectId.getName();
		Repository repository = commit.getRepository();

		ObjectId headId;
		try {
			headId = repository.resolve(Constants.HEAD);
		} catch (RevisionSyntaxException | IOException e) {
			logger.error(e.getMessage(), e);
			return false;
		}
		try (RevWalk walk = new RevWalk(repository)) {
			walk.markStart(walk.parseCommit(headId));
			RevCommit ancestor;
			boolean isAncestor = false;
			while ((ancestor = walk.next()) != null) {
				if (ancestor.getId().equals(objectId)) {
					isAncestor = true;
					break;
				}
			}
			if (!isAncestor) {
				return false;
			}
		} catch (IOException e1) {
			logger.error(e1.getMessage(), e1);
		}

		String hexshaHead = headId.getName();
		return !hexshaCommit.equals(hexshaHead);
	}

}
