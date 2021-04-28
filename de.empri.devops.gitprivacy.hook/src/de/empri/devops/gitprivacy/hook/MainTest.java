package de.empri.devops.gitprivacy.hook;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import de.empri.devops.gitprivacy.hook.ProvidesCommitDate.CommitDateResult;
import de.empri.devops.gitprivacy.hook.shared.Crypto;
import de.empri.devops.gitprivacy.hook.shared.ManagesKeyStorage;
import de.empri.devops.gitprivacy.hook.shared.OriginalCommitDateEncoder;

public class MainTest {
	private static final ProvidesCommitDate PROVIDES_COMMIT_DATE = new ProvidesCommitDate();

	public static void main(String[] args) {
		Git git;
		try {
			String repoPath;
			if (args.length > 0 && args[0].equals("-dev")) {
				repoPath = System.getProperty("user.home") + File.separator + "git" + File.separator + "hooktesting";
			} else {
				repoPath = ".";
			}
			File repoDir = new File(repoPath);
			git = Git.open(repoDir);
			Repository repository = git.getRepository();
			ObjectId lastCommitId = repository.resolve(Constants.HEAD);
			RevWalk revWalk = new RevWalk(repository);
			RevCommit lastCommit = revWalk.parseCommit(lastCommitId);
			revWalk.close();
			PersonIdent authorIdent = lastCommit.getAuthorIdent();
			PersonIdent committerIdent = lastCommit.getCommitterIdent();
			CommitDateResult commitDateResult = PROVIDES_COMMIT_DATE.commitDate(repository.getConfig());

			if (!commitDateResult.isRedated()) {
				return;
			}

			List<String> allKeys = new ManagesKeyStorage(new File(repoDir, ".git")).readAllKeys();
			String commitMessage;
			if (allKeys.isEmpty()) {
				commitMessage = lastCommit.getFullMessage();
			} else {
				OriginalCommitDateEncoder encoder = new OriginalCommitDateEncoder(new Crypto(allKeys));
				commitMessage = encoder.encode(lastCommit.getFullMessage(), commitDateResult.getOriginalCommitDate());
			}
			RevCommit revCommit = null;
			try {
				revCommit = git.commit()
						.setAmend(true)
						.setMessage(commitMessage)
						.setAuthor(new PersonIdent(authorIdent, commitDateResult.getCommitDate()))
						.setCommitter(new PersonIdent(committerIdent, commitDateResult.getCommitDate()))
						.call();
			} catch (GitAPIException e) {
				e.printStackTrace();
			}
			System.out.println(revCommit);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
