package de.empri.devops.gitprivacy.preferences;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.egit.ui.internal.repository.tree.RepositoryNode;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

public class InitializeCommandHandler extends AbstractHandler {

	private ProcessService processService;
	private ILog logger;

	public InitializeCommandHandler() {
		processService = new ProcessService();
		logger = Platform.getLog(getClass());
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
		List<RepositoryNode> list = null;
		if (selection instanceof IStructuredSelection) {
			list = ((IStructuredSelection) selection).toList();
		}
		if (list == null || list.size() != 1) {
			// TODO(FAP): show error dialog?
			return null;
		}
		RepositoryNode node = list.get(0);
		try {
			Process process = processService.start("git-privacy init", node.getRepository().getDirectory()); //$NON-NLS-1$
			Shell activeShell = Display.getCurrent().getActiveShell();
			try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
				String message = input.lines().collect(Collectors.joining(System.lineSeparator()));
				if (!message.isBlank()) {
					MessageDialog.openError(activeShell, UIText.InitializeCommandHandler_ErrorDialog_Title,
							message);
					return null;
				}
			}
			String message = ""; //$NON-NLS-1$
			try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				message = input.lines().collect(Collectors.joining(System.lineSeparator()));
				logger.info(message);
			}
			MessageDialog.openInformation(activeShell, UIText.InitializeCommandHandler_SuccessDialog_Title, message);
		} catch (IOException e) {
			// TODO git-privacy not installed?
			e.printStackTrace();
		}
		return null;
	}

}
