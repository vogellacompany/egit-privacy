
package de.empri.devops.gitprivacy.preferences;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.inject.Named;

import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.egit.core.internal.IRepositoryCommit;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ui.history.GenericHistoryView;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

public class RedateAllFollowingCommitsHandler {

	private static final int PROCESS_TIMEOUT_IN_SEC = 30;
	private ProcessService processService;
	private ILog logger;
	private boolean withForce;

	public RedateAllFollowingCommitsHandler() {
		processService = new ProcessService();
		logger = Platform.getLog(getClass());
	}

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell s,
			@org.eclipse.e4.core.di.annotations.Optional @Named(IServiceConstants.ACTIVE_SELECTION) Object o,
			UISynchronize sync) {
		if (!(o instanceof IStructuredSelection)) {
			return;
		}
		Object selection = ((IStructuredSelection) o).getFirstElement();
		if (!(selection instanceof IRepositoryCommit)) {
			return;
		}

		Job job = Job.create(UIText.RedateAllFollowingCommitsHandler_redateJobTitle, (ICoreRunnable) monitor -> {
			IRepositoryCommit commit = (IRepositoryCommit) selection;
			ObjectId objectId = commit.getObjectId();

			String branchName = null;
			Repository repository = commit.getRepository();
			try {
				branchName = repository.getBranch();
			} catch (IOException e) {
				logger.error(e.getMessage(), e); // $NON-NLS-1$
				return;
			}
			withForce = false;
			try (Git git = new Git(repository)) {

				Iterable<RevCommit> logs = git.log()
						.addRange(repository.resolve(objectId.getName()), repository.resolve(Constants.HEAD)).call();
				RevCommit nextFollowingCommit = StreamSupport.stream(logs.spliterator(), false)
						.reduce((prev, next) -> next).orElse(null);

				List<Ref> remoteRefsContainingCommit = git.branchList().setContains(nextFollowingCommit.getName())
						.setListMode(ListBranchCommand.ListMode.REMOTE).call();
				HashSet<Boolean> cancel = new HashSet<>();
				if (!remoteRefsContainingCommit.isEmpty()) {
					sync.syncExec(() -> {
						int redate = MessageDialog.open(MessageDialog.QUESTION, s, UIText.RedateAllFollowingCommitsHandler_alreadyPushedToRemoteDialog_title,
								UIText.RedateAllFollowingCommitsHandler_alreadyPushedToRemoteDialog_message,
								SWT.NONE, UIText.RedateAllFollowingCommitsHandler_alreadyPushedToRemoteDialog_redateButton, UIText.RedateAllFollowingCommitsHandler_alreadyPushedToRemoteDialog_dontRedateButton);
						if (redate != 0) {
							cancel.add(true);
							return;
						} else {
							withForce = true;
						}
					});
					if (cancel.contains(true)) {
						return;
					}
				}
			} catch (GitAPIException | RevisionSyntaxException | IOException e) {
				logger.error(e.getMessage(), e); // $NON-NLS-1$
				return;
			}

			String command = "git-privacy redate " + objectId.getName(); //$NON-NLS-1$
			if (withForce) {
				command += " -f"; //$NON-NLS-1$
			}
			try {
				Process process = processService.start(command, repository.getDirectory().getParentFile(), false);
				boolean waitFor = process.waitFor(PROCESS_TIMEOUT_IN_SEC, TimeUnit.SECONDS);
				if (!waitFor) {
					process.destroy();
				}
				String error = new BufferedReader(new InputStreamReader(process.getErrorStream())).lines()
						.collect(Collectors.joining("\n")); //$NON-NLS-1$
				if (error.length() > 0) {
					sync.asyncExec(() -> {
						MessageDialog.openError(s, UIText.RedateAllFollowingCommitsHandler_redateErrorDialog_title, error);
					});
					return;
				}
			} catch (InterruptedException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			sync.asyncExec(() -> {
				refreshHistoryView();
			});
		});

		job.schedule();

	}

	private void refreshHistoryView() {
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IWorkbenchPart activePart = activePage.getActivePart();
		if (activePart instanceof GenericHistoryView) {
			((GenericHistoryView) activePart).getViewSite().getActionBars()
					.getGlobalActionHandler(ActionFactory.REFRESH.getId()).run();
		}
	}

}