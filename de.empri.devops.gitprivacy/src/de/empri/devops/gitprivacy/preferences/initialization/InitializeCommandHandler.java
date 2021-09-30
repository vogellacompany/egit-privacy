package de.empri.devops.gitprivacy.preferences.initialization;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.egit.ui.internal.repository.tree.RepositoryNode;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.empri.devops.gitprivacy.preferences.ProcessService;
import de.empri.devops.gitprivacy.preferences.UIText;
import de.empri.devops.gitprivacy.preferences.initialization.HookParseStateMachine.ErrorValue;

public class InitializeCommandHandler {

	private ProcessService processService;
	private ILog logger;

	public InitializeCommandHandler() {
		processService = new ProcessService();
		logger = Platform.getLog(getClass());
	}

	public class State {
		public BufferedReader br;
		public String currentLine;
		public HookParseStateMachine parser;
		public List<ErrorValue> errors = new ArrayList<>();
		public List<String> newlyInstalled = new ArrayList<>();
		public List<String> alreadyInstalled = new ArrayList<>();

		public void nextLine() throws IOException {
			currentLine = br.readLine();
		}

		public ErrorValue currentError() {
			return errors.get(errors.size() - 1);
		}
	}
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional Object selection)
			throws ExecutionException {
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
		try {
			// we're using script to force the merging of stdout and syserr without any
			// caching, giving us the correct chronological order
			Process process = processService.start("script -q -c \"git-privacy init\" /dev/null",
					node.getRepository().getDirectory());
			Shell activeShell = Display.getCurrent().getActiveShell();
			State state = new State();
			try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				state.parser = HookParseStateMachine.INITIAL;
				state.br = input;
				while (state.parser != HookParseStateMachine.FINISHED) {
					state = state.parser.parse(state);
				}
			}
			List<String> newlyInstalled = state.newlyInstalled;
			List<ErrorValue> errors = state.errors;
			if (!errors.isEmpty()) {
				new InitializeErrorDialog(activeShell, state, node.getRepository().getDirectory().toPath()).open();
				return;
			}

			StringBuilder sb = new StringBuilder();
			for (String hookName : state.newlyInstalled) {
				sb.append("- ").append(hookName).append("\n");
			}
			MessageDialog.openInformation(activeShell, UIText.InitializeCommandHandler_SuccessDialog_Title,
					NLS.bind(UIText.InitializeCommandHandler_SuccessDialog_Message, sb.toString()));
		} catch (IOException e) {
			// TODO git-privacy not installed?
			e.printStackTrace();
		}
		return;
	}

}
