package de.empri.devops.gitprivacy.preferences.initialization;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.io.IOException;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.egit.core.RepositoryUtil;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.widgets.WidgetFactory;
import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.FrameworkUtil;

import de.empri.devops.gitprivacy.preferences.UIText;

public class InitializeGitPrivacyConfigDialog extends TitleAreaDialog {

	private Button modifyCommitDate;
	private Button modifyCommitMonth;
	private Button modifyCommitDay;
	private Button modifyCommitHour;
	private Button modifyCommitMinute;
	private Button modifyCommitSecond;
	private Button limitCommitTime;
	private Text lowerLimit;
	private Text upperLimit;
	private StoredConfig gitConfig;
	private Image image;
	private Repository repository;
	private ILog logger;

	protected InitializeGitPrivacyConfigDialog(Shell parentShell, Repository repository) {
		super(parentShell);
		this.repository = repository;
		this.gitConfig = repository.getConfig();
		logger = Platform.getLog(getClass());
		setHelpAvailable(false);
	}

	@Override
	public void create() {
		super.create();
		setTitle(NLS.bind(UIText.InitializeGitPrivacyConfigDialog_titleArea_title,
				RepositoryUtil.INSTANCE.getRepositoryName(repository)));
		image = ImageDescriptor.createFromURL(FrameworkUtil.getBundle(getClass()).getEntry("icons/empri-logo.png")) //$NON-NLS-1$
				.createImage();
		setTitleImage(image);
		setMessage(UIText.InitializeGitPrivacyConfigDialog_titleArea_message, IMessageProvider.INFORMATION);
	}

	@Override
	public boolean close() {
		if (image != null) {
			image.dispose();
		}
		return super.close();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(UIText.InitializeGitPrivacyConfigDialog_title);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, UIText.InitializeGitPrivacyConfigDialog_okayButton, true);
		createButton(parent, IDialogConstants.CANCEL_ID, UIText.InitializeGitPrivacyConfigDialog_cancelButton, false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		initializeDialogUnits(parent);

		GridLayout layout = new GridLayout();
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.numColumns = 1;
		Composite main = WidgetFactory.composite(SWT.NONE).layout(layout).create(parent);
		applyDialogFont(main);
		GridDataFactory.fillDefaults().indent(0, 0)
				.minSize(convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH), 0).grab(true, true)
				.applyTo(main);


		modifyCommitDate = createCheckBox(main, UIText.PrivacyPreferencePage_modify_commit_date);
		modifyCommitDate.setSelection(true);
		GridDataFactory.fillDefaults().applyTo(modifyCommitDate);
		modifyCommitDate.addSelectionListener(widgetSelectedAdapter(e -> {
			modifyCommitMonth.setEnabled(modifyCommitDate.getSelection());
			modifyCommitDay.setEnabled(modifyCommitDate.getSelection());
			modifyCommitHour.setEnabled(modifyCommitDate.getSelection());
			modifyCommitMinute.setEnabled(modifyCommitDate.getSelection());
			modifyCommitSecond.setEnabled(modifyCommitDate.getSelection());
			verifyInput();
		}));

		modifyCommitMonth = createCheckBox(main, UIText.PrivacyPreferencePage_modify_commit_month);
		GridDataFactory.fillDefaults().indent(LayoutConstants.getIndent(), 0)
				.applyTo(modifyCommitMonth);
		modifyCommitMonth.setEnabled(modifyCommitDate.getSelection());
		modifyCommitMonth.addSelectionListener(widgetSelectedAdapter(e -> verifyInput()));

		modifyCommitDay = createCheckBox(main, UIText.PrivacyPreferencePage_modify_commit_day);
		GridDataFactory.fillDefaults().indent(LayoutConstants.getIndent(), 0).applyTo(modifyCommitDay);
		modifyCommitDay.setEnabled(modifyCommitDate.getSelection());
		modifyCommitDay.addSelectionListener(widgetSelectedAdapter(e -> verifyInput()));

		modifyCommitHour = createCheckBox(main, UIText.PrivacyPreferencePage_modify_commit_hour);
		modifyCommitHour.setSelection(true);
		GridDataFactory.fillDefaults().indent(LayoutConstants.getIndent(), 0).applyTo(modifyCommitHour);
		modifyCommitHour.setEnabled(modifyCommitDate.getSelection());
		modifyCommitHour.addSelectionListener(widgetSelectedAdapter(e -> verifyInput()));

		modifyCommitMinute = createCheckBox(main, UIText.PrivacyPreferencePage_modify_commit_minute);
		modifyCommitMinute.setSelection(true);
		GridDataFactory.fillDefaults().indent(LayoutConstants.getIndent(), 0).applyTo(modifyCommitMinute);
		modifyCommitMinute.setEnabled(modifyCommitDate.getSelection());
		modifyCommitMinute.addSelectionListener(widgetSelectedAdapter(e -> verifyInput()));

		modifyCommitSecond = createCheckBox(main, UIText.PrivacyPreferencePage_modify_commit_second);
		modifyCommitSecond.setSelection(true);
		GridDataFactory.fillDefaults().indent(LayoutConstants.getIndent(), 0).applyTo(modifyCommitSecond);
		modifyCommitSecond.setEnabled(modifyCommitDate.getSelection());
		modifyCommitSecond.addSelectionListener(widgetSelectedAdapter(e -> verifyInput()));

		limitCommitTime = createCheckBox(main, UIText.PrivacyPreferencePage_limit_commit_time);
		limitCommitTime.addSelectionListener(widgetSelectedAdapter(e -> {
			lowerLimit.setEnabled(limitCommitTime.getSelection());
			upperLimit.setEnabled(limitCommitTime.getSelection());
			verifyInput();
		}));

		Label label = new Label(main, SWT.NONE);
	    label.setText(UIText.PrivacyPreferencePage_lower_commit_time_limit);
		GridDataFactory.fillDefaults().indent(LayoutConstants.getIndent(), 0).applyTo(label);
		lowerLimit = new Text(main, SWT.BORDER);
		GridDataFactory.fillDefaults().indent(LayoutConstants.getIndent(), 0).applyTo(lowerLimit);
		lowerLimit.setEnabled(limitCommitTime.getSelection());
		lowerLimit.addModifyListener(e -> verifyInput());

		label = new Label(main, SWT.NONE);
		label.setText(UIText.PrivacyPreferencePage_upper_commit_time_limit);
		GridDataFactory.fillDefaults().indent(LayoutConstants.getIndent(), 0).applyTo(label);
		upperLimit = new Text(main, SWT.BORDER);
		GridDataFactory.fillDefaults().indent(LayoutConstants.getIndent(), 0).applyTo(upperLimit);
		upperLimit.setEnabled(limitCommitTime.getSelection());
		upperLimit.addModifyListener(e -> verifyInput());

		return main;
	}

	private void verifyInput() {
		if (modifyCommitDate.getSelection()) {
			if (!(modifyCommitMonth.getSelection()
					|| modifyCommitDay.getSelection()
					|| modifyCommitHour.getSelection()
					|| modifyCommitMinute.getSelection()
					|| modifyCommitSecond.getSelection())) {
				setErrorMessage(UIText.InitializeGitPrivacyConfigDialog_noTimeUnitForRedactionChosenErrorMessage);
				return;
			}
		}
		if (limitCommitTime.getSelection()) {
			String lowerLimitText = lowerLimit.getText();
		    String upperLimitText = upperLimit.getText();
			if (!lowerLimitText.matches("-?\\d+") || Integer.valueOf(lowerLimitText) < 0 || Integer.valueOf(lowerLimitText) > 24) { //$NON-NLS-1$
				setErrorMessage(UIText.InitializeGitPrivacyConfigDialog_lowerLimitErrorMessage);
				return;
			} else if (upperLimit.getText().matches("-?\\d+") //$NON-NLS-1$
					&& Integer.valueOf(upperLimit.getText()) < Integer.valueOf(lowerLimitText)) {
				setErrorMessage(UIText.InitializeGitPrivacyConfigDialog_lowerLimitLEQErrorMessage);
				return;
			} else if (!upperLimitText.matches("-?\\d+") || Integer.valueOf(upperLimitText) < 0 //$NON-NLS-1$
					|| Integer.valueOf(upperLimitText) > 24) {
				setErrorMessage(UIText.InitializeGitPrivacyConfigDialog_upperLimitErrorMessage);
				return;
			} else if (lowerLimit.getText().matches("-?\\d+") //$NON-NLS-1$
					&& Integer.valueOf(lowerLimit.getText()) > Integer.valueOf(upperLimitText)) {
				setErrorMessage(UIText.InitializeGitPrivacyConfigDialog_upperLimitGTEErrorMessage);
				return;
			}
		}

		setErrorMessage(null);
	}


	@Override
	public void setErrorMessage(String newErrorMessage) {
		super.setErrorMessage(newErrorMessage);
		boolean errorMessageSet = newErrorMessage != null;
		getButton(IDialogConstants.OK_ID).setEnabled(!errorMessageSet);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			try {
				gitConfig.load();
			} catch (IOException | ConfigInvalidException e1) {
				logger.error(e1.getMessage(), e1);
			}
			if (modifyCommitDate.getSelection()) {
				StringBuilder sb = new StringBuilder();
				if (modifyCommitMonth.getSelection()) {
					sb.append("M"); //$NON-NLS-1$
				}
				if (modifyCommitDay.getSelection()) {
					sb.append("d"); //$NON-NLS-1$
				}
				if (modifyCommitHour.getSelection()) {
					sb.append("h"); //$NON-NLS-1$
				}
				if (modifyCommitMinute.getSelection()) {
					sb.append("m"); //$NON-NLS-1$
				}
				if (modifyCommitSecond.getSelection()) {
					sb.append("s"); //$NON-NLS-1$
				}
				gitConfig.setString("privacy", null, "pattern", sb.toString()); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (limitCommitTime.getSelection()) {
				StringBuilder sb = new StringBuilder();
				sb.append(lowerLimit.getText());
				sb.append("-"); //$NON-NLS-1$
				sb.append(upperLimit.getText());
				gitConfig.setString("privacy", null, "limit", sb.toString()); //$NON-NLS-1$ //$NON-NLS-2$
			}
			try {
				gitConfig.save();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		super.buttonPressed(buttonId);
	}

	private Button createCheckBox(Composite parent, String label) {
		Button checkBox = new Button(parent, SWT.CHECK | SWT.LEFT);
		checkBox.setFont(parent.getFont());
		checkBox.setText(label);
		return checkBox;
	}

}
