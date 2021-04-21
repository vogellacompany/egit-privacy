package de.empri.devops.gitprivacy.hook;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;

public class MainTest {
	public static void main(String[] args) {
		Git git;
		try {
			git = Git.open(new File("/home/vogella/git/hooktesting/.git/"));
			Repository repository = git.getRepository();
			List<Ref> refs = repository.getRefDatabase().getRefs();
			System.out.println(refs);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
