 
package de.empri.devops.gitprivacy.preferences;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;

import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.egit.ui.internal.repository.tree.RepositoryNode;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;

public class RedactEmailHandler {

	private static final long PROCESS_TIMEOUT_IN_SEC = 60 * 10;
	private ProcessService processService;
	private ILog logger;

	public RedactEmailHandler() {
		processService = new ProcessService();
		logger = Platform.getLog(getClass());
	}

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell s,
			@Named(IServiceConstants.ACTIVE_SELECTION) @Optional Object selection) {
		if (!(selection instanceof ISelection)) {
			return;
		}
		ISelection iSelection = (ISelection) selection;
		List<RepositoryNode> list = null;
		if (selection instanceof IStructuredSelection) {
			list = ((IStructuredSelection) selection).toList();
		}
		if (list == null || list.size() != 1) {
			// TODO(FAP): show error dialog?
			return;
		}
		RepositoryNode node = list.get(0);

		RedactEmailDialog redactEmailDialog = new RedactEmailDialog(s, node.getRepository());
		int returnCode = redactEmailDialog.open();
		if (returnCode == IDialogConstants.OK_ID) {
			Job job = Job.create("Redact E-Mail Addresses", (ICoreRunnable) monitor -> {
				StringBuilder sb = new StringBuilder();
				sb.append("git-privacy redact-email");
				redactEmailDialog.toRedact().forEach(email -> {
					sb.append(" ");
					sb.append(email.address);
					if (!email.replacement.isBlank()) {
						sb.append(":");
						sb.append(email.replacement);
					}
				});
				try {
					Process process = processService.start(sb.toString(),
							node.getRepository().getDirectory());
					process.waitFor(PROCESS_TIMEOUT_IN_SEC, TimeUnit.SECONDS);
					try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
						System.out.println(input);
					}
					try (BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
						System.out.println(error);
					}
				} catch (IOException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			job.schedule();
		}
	}
		
}