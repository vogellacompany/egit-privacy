package de.empri.devops.gitprivacy.hook;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

public class MainTest {
	public static void main(String[] args) {
		Git git;
		try {
			String repoPath;
			if (args.length > 0 && args[0].equals("-dev")) {
				repoPath = System.getProperty("user.home") + "/git/hooktesting/.git/";
			} else {
				repoPath = "../";
			}
			git = Git.open(new File(repoPath));
			Repository repository = git.getRepository();
			ObjectId lastCommitId = repository.resolve(Constants.HEAD);
			RevWalk revWalk = new RevWalk(repository);
			RevCommit lastCommit = revWalk.parseCommit(lastCommitId);
			PersonIdent authorIdent = lastCommit.getAuthorIdent();
			PersonIdent committerIdent = lastCommit.getCommitterIdent();
			Date date = new Date(0);
			RevCommit revCommit = null;
			try {
				revCommit = git.commit()
						.setAmend(true)
						.setMessage(lastCommit.getFullMessage())
						.setAuthor(new PersonIdent(authorIdent, date))
						.setAuthor(new PersonIdent(committerIdent, date))
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

//package de.empri.devops.gitprivacy.hook;
//
//import java.io.File;
//import java.io.IOException;
//
//import org.eclipse.jgit.api.Git;
//import org.eclipse.jgit.api.errors.GitAPIException;
//import org.eclipse.jgit.lib.Constants;
//import org.eclipse.jgit.lib.ObjectId;
//import org.eclipse.jgit.lib.PersonIdent;
//import org.eclipse.jgit.lib.Repository;
//import org.eclipse.jgit.revwalk.RevCommit;
//import org.eclipse.jgit.revwalk.RevWalk;
//
//import de.empri.devops.gitprivacy.hook.ProvidesCommitDate.CommitDateResult;
//
//public class MainTest {
//	public static void main(String[] args) {
//		Git git;
//		try {
//			git = Git.open(new File(System.getProperty("user.home") + "/git/hooktesting/.git/"));
//			Repository repository = git.getRepository();
//
//			ProvidesCommitDate providesCommitDate = new ProvidesCommitDate();
//			CommitDateResult commitDateResult = providesCommitDate.commitDate(repository.getConfig());
//			if (commitDateResult.isRedated()) {
//				ObjectId lastCommitId = repository.resolve(Constants.HEAD);
//				RevWalk revWalk = new RevWalk(repository);
//				RevCommit lastCommit = revWalk.parseCommit(lastCommitId);
//				PersonIdent authorIdent = lastCommit.getAuthorIdent();
//				PersonIdent committerIdent = lastCommit.getCommitterIdent();
//				RevCommit revCommit = null;
//				try {
//					revCommit = git.commit()
//							.setAmend(true)
//							.setMessage(lastCommit.getFullMessage())
//							.setAuthor(new PersonIdent(authorIdent, commitDateResult.getCommitDate()))
//							.setAuthor(new PersonIdent(committerIdent, commitDateResult.getCommitDate()))
//							.call();
//				} catch (GitAPIException e) {
//					e.printStackTrace();
//				}
//				System.out.println(revCommit);
//			}
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//}
