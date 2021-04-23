package de.empri.devops.gitprivacy.preferences;

import org.eclipse.egit.ui.internal.clone.GitSelectRepositoryPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jgit.lib.Repository;

public class PasswordMigrationWizard extends Wizard {

	private GitSelectRepositoryPage page;

	@Override
	public boolean performFinish() {
		Repository repositories = page.getRepository();
		System.out.println(repositories);
		return false;
	}

	@Override
	public boolean canFinish() {
		return page.getRepository() != null;
	}

	@Override
	public void addPages() {
		super.addPages();
		page = new GitSelectRepositoryPage(true, true);
		addPage(page);
	}

}
