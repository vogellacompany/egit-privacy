package de.empri.devops.gitprivacy.preferences;

import java.util.List;
import java.util.Optional;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import de.empri.devops.gitprivacy.preferences.shared.Crypto;
import de.empri.devops.gitprivacy.preferences.shared.ManagesKeyStorage;
import de.empri.devops.gitprivacy.preferences.shared.OriginalCommitDateEncoder;
import de.empri.devops.gitprivacy.preferences.shared.OriginalCommitDateEncoder.DecodedDates;

public class ShowOriginalCommitDateDialog extends Dialog {

	private RevCommit commit;
	private Repository repository;

	protected ShowOriginalCommitDateDialog(Shell parentShell) {
		super(parentShell);
	}

	public ShowOriginalCommitDateDialog(Shell parentShell, Repository repository, RevCommit commit) {
		super(parentShell);
		this.repository = repository;
		this.commit = commit;
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().applyTo(main);
		GridDataFactory.fillDefaults().indent(0, 0).grab(true, true).applyTo(main);
		Group commitDateGroup = new Group(main, SWT.SHADOW_ETCHED_IN);
		commitDateGroup.setText("Original Commit Date");
		GridLayoutFactory.fillDefaults().applyTo(commitDateGroup);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(commitDateGroup);
		GridLayoutFactory.swtDefaults().applyTo(commitDateGroup);
		Label commitDate = new Label(commitDateGroup, SWT.NONE);
		Group authorDateGroup = new Group(main, SWT.SHADOW_ETCHED_IN);
		authorDateGroup.setText("Original Author Date");
		GridLayoutFactory.fillDefaults().applyTo(authorDateGroup);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(authorDateGroup);
		GridLayoutFactory.swtDefaults().applyTo(authorDateGroup);
		Label authorDate = new Label(authorDateGroup, SWT.NONE);

		List<String> allKeys = new ManagesKeyStorage(repository.getDirectory()).readAllKeys();
		if (!allKeys.isEmpty()) {
			OriginalCommitDateEncoder encoder = new OriginalCommitDateEncoder(new Crypto(allKeys));
			Optional<DecodedDates> decode = encoder.decode(commit.getFullMessage());
			if (decode.isPresent()) {
				commitDate.setText(decode.get().getCommittedDateTime().toString());
				authorDate.setText(decode.get().getAuthoredDateTime().toString());
			}
		}

		return main;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button okayButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.CLOSE_LABEL, true);
		okayButton.setFocus();
	}

}
