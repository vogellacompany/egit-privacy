package de.empri.devops.gitprivacy.preferences.initialization;

import java.io.File;
import java.nio.file.Path;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IconAndMessageDialog;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ExpandEvent;
import org.eclipse.swt.events.ExpandListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Shell;

import de.empri.devops.gitprivacy.preferences.UIText;
import de.empri.devops.gitprivacy.preferences.initialization.HookParseStateMachine.ErrorValue;
import de.empri.devops.gitprivacy.preferences.initialization.InitializeCommandHandler.State;

public class InitializeErrorDialog extends IconAndMessageDialog {

	public static final int OPEN_HOOK_DIRECTORY_ID = 1337;
	private State state;
	private String hooksDirectory;

	protected InitializeErrorDialog(Shell parentShell) {
		super(parentShell);
	}

	public InitializeErrorDialog(Shell activeShell, State state, Path gitMetaDirectory) {
		this(activeShell);
		this.state = state;
		this.hooksDirectory = gitMetaDirectory.toString() + File.separator + "hooks";
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, OPEN_HOOK_DIRECTORY_ID, "&Open Hooks Dir", false);
	}

	@Override
	protected void buttonPressed(int id) {
		if (id == OPEN_HOOK_DIRECTORY_ID) {
			openHookDirectory();
		} else {
			super.buttonPressed(id);
		}
	}

	private void openHookDirectory() {
		Program.launch(hooksDirectory);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		StringBuilder sb = new StringBuilder();
		if (!state.newlyInstalled.isEmpty()) {
			sb.append("Git hooks successfully initialized for:\n");
			for (String hookName : state.newlyInstalled) {
				sb.append("- ")
					.append(hookName)
					.append("\n");
			}
			sb.append("\n");
		}
		if (!state.alreadyInstalled.isEmpty()) {
			sb.append("The following Git hooks where already in place:\n");
			for (String hookName : state.alreadyInstalled) {
				sb.append("- ").append(hookName).append("\n");
			}
			sb.append("\n");
		}
		if (!state.errors.isEmpty()) {
			sb.append(
					"The following Git hooks failed to be initialized as there where already other hooks in place:\n");
			for (String hookName : state.alreadyInstalled) {
				sb.append("- ").append(hookName).append("\n");
			}
			sb.append("\n");
			sb.append("To fully initialize git-privacy add the lines below to the respective hook files.\n");
		}
		message = sb.toString();
		super.createMessageArea(main);
		GridLayout layout = new GridLayout();
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING) * 2;
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.numColumns = 2;
		main.setLayout(layout);
		ExpandBar bar = new ExpandBar(main, SWT.NONE);
//		bar.setSpacing(5);
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.verticalSpan = 1;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		bar.setLayoutData(gridData);

		for (ErrorValue errorValue : state.errors) {
			Composite composite = new Composite(bar, SWT.NONE);
			FillLayout fillLayout = new FillLayout();
			composite.setLayout(fillLayout);
			TextViewer textViewer = new TextViewer(composite, SWT.BORDER);
			textViewer.setEditable(false);
			textViewer.setDocument(new Document(errorValue.fix));
			StyledText styledText = textViewer.getTextWidget();
			int margin = 3;
			styledText.setMargins(margin, margin, margin, margin);
			ExpandItem item0 = new ExpandItem(bar, SWT.READ_ONLY, 0);
			item0.setText(errorValue.hookName);
			item0.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
			item0.setControl(composite);
			
			bar.addExpandListener(new ExpandListener() {

				@Override
				public void itemExpanded(ExpandEvent e) {
					parent.getDisplay().asyncExec(() -> {
//						item0.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
						parent.pack();
					});
				}

				@Override
				public void itemCollapsed(ExpandEvent e) {
					parent.getDisplay().asyncExec(() -> {
//						item0.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
						parent.pack();
					});

				}
			});
		}

		return main;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		String title = null;
		if (state.alreadyInstalled.isEmpty()) {
			title = UIText.InitializeCommandHandler_ErrorDialog_Title;
		} else {
			title = "EGit-Privacy initialization partially failed";
		}
		newShell.setText(title);
	}

	@Override
	protected Image getImage() {
		return getErrorImage();
	}

}
