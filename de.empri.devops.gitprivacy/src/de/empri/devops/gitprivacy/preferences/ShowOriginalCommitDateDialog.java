package de.empri.devops.gitprivacy.preferences;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.GitDateFormatter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import de.empri.devops.gitprivacy.preferences.shared.OriginalCommitDateEncoder;
import de.empri.devops.gitprivacy.preferences.shared.OriginalCommitDateEncoder.DecodedDates;
import de.empri.devops.gitprivacy.preferences.shared.OriginalCommitDateEncoderFactory;

public class ShowOriginalCommitDateDialog extends Dialog {

	private static final String GIT_ISO_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss Z";
	private static final DateTimeFormatter ISO_DATET_TIME_FORMATTER = DateTimeFormatter.ofPattern(GIT_ISO_TIME_PATTERN);
	private static final GitDateFormatter ISO_DATE_FORMATTER = new GitDateFormatter(GitDateFormatter.Format.ISO);

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

		Group authorDateGroup = new Group(main, SWT.SHADOW_ETCHED_IN);
		authorDateGroup.setText("Auhtored Date");
		GridLayoutFactory.fillDefaults().applyTo(authorDateGroup);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(authorDateGroup);
		GridLayoutFactory.swtDefaults().applyTo(authorDateGroup);
		Label authorDate = new Label(authorDateGroup, SWT.NONE);
		authorDate.setText(ISO_DATE_FORMATTER.formatDate(commit.getAuthorIdent()));

		Group realAuthorDateGroup = new Group(main, SWT.SHADOW_ETCHED_IN);
		realAuthorDateGroup.setText("Real Authored Date");
		GridLayoutFactory.fillDefaults().applyTo(realAuthorDateGroup);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(realAuthorDateGroup);
		GridLayoutFactory.swtDefaults().applyTo(realAuthorDateGroup);
		Label realAuthorDate = new Label(realAuthorDateGroup, SWT.NONE);

		Group commitDateGroup = new Group(main, SWT.SHADOW_ETCHED_IN);
		commitDateGroup.setText("Committed Date");
		GridLayoutFactory.fillDefaults().applyTo(commitDateGroup);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(commitDateGroup);
		GridLayoutFactory.swtDefaults().applyTo(commitDateGroup);
		Label commitDate = new Label(commitDateGroup, SWT.NONE);
		commitDate.setText(ISO_DATE_FORMATTER.formatDate(commit.getCommitterIdent()));

		Group realCommitDateGroup = new Group(main, SWT.SHADOW_ETCHED_IN);
		realCommitDateGroup.setText("Real Committed Date");
		GridLayoutFactory.fillDefaults().applyTo(realCommitDateGroup);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(realCommitDateGroup);
		GridLayoutFactory.swtDefaults().applyTo(realCommitDateGroup);
		Label realCommitDate = new Label(realCommitDateGroup, SWT.NONE);

		DateTimeFormatter.ofPattern(GIT_ISO_TIME_PATTERN);
		Optional<OriginalCommitDateEncoder> encoderOptional = OriginalCommitDateEncoderFactory.build(repository);
		if (encoderOptional.isPresent()) {
			Optional<DecodedDates> decode = encoderOptional.get().decode(commit.getFullMessage());
			if (decode.isPresent()) {
				
				realCommitDate.setText(ISO_DATET_TIME_FORMATTER.format(decode.get().getCommittedDateTime()));
				realAuthorDate.setText(ISO_DATET_TIME_FORMATTER.format(decode.get().getAuthoredDateTime()));
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
