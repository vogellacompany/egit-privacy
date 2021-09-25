package de.empri.devops.gitprivacy.preferences;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.egit.ui.Activator;
import org.eclipse.egit.ui.internal.SWTUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;

import de.empri.devops.gitprivacy.preferences.shared.Crypto;
import de.empri.devops.gitprivacy.preferences.shared.GitPrivacyConfigReader;
import de.empri.devops.gitprivacy.preferences.shared.ManagesKeyStorage;

public class GitPrivacyPropertyPage extends PropertyPage {

	private Clipboard clipboard;

	private MessageDialog migrateFromGitConfigConfirmationDialog;

	private MessageDialog archiveKeyConfirmationDialog;

	private GitPrivacyConfigReader configReader = new GitPrivacyConfigReader();

	private MessageDialog eclipseOrGitDialog;

	private MessageDialog eclipseDialog;
	
	@Override
	protected Control createContents(Composite parent) {
		Composite composite = SWTUtils.createHVFillComposite(parent,
				SWTUtils.MARGINS_NONE);

		Repository repo = Adapters.adapt(getElement(), Repository.class);
		if (repo == null) {
			return composite;
		}
		Config config = repo.getConfig();


		ManagesKeyStorage managesKeyStorage = new ManagesKeyStorage(
				repo.getDirectory());
		boolean currentKeyExists = managesKeyStorage.readCurrentKey()
				.isPresent();

		PasswordToKeyMigration passwordToKeyMigration = new PasswordToKeyMigration(parent.getShell());

		GridData buttonLayoutData = new GridData(300, SWT.DEFAULT);
		// TODO(FAP): should listen to key creation for button text change
		Button initKeyButton = new Button(composite, SWT.NONE);
		if (currentKeyExists) {
			initKeyButton.setText(UIText.GitPrivacyPage_ReplaceKeyButton_Text);
			initKeyButton.addSelectionListener(widgetSelectedAdapter(e -> {
				try {
					String key = Crypto.generateKey();
					passwordToKeyMigration.replaceKey(managesKeyStorage, key);
				} catch (IOException ex) {
					Activator.handleError(ex.getMessage(), ex, true);
				}
			}));
		} else {
			initKeyButton.setText(UIText.GitPrivacyPage_GenerateKeyButton_Text);
			initKeyButton.addSelectionListener(widgetSelectedAdapter(e -> {
				try {
					managesKeyStorage.store(Crypto.generateKey());
					initKeyButton.setText(UIText.GitPrivacyPage_ReplaceKeyButton_Text);
				} catch (IOException ex) {
					Activator.handleError(ex.getMessage(), ex, true);
				}
			}));
		}
		initKeyButton.setLayoutData(buttonLayoutData);

		Button copyCurrentKeyButton = new Button(composite, SWT.NONE);
		clipboard = new Clipboard(parent.getDisplay());
		copyCurrentKeyButton.setText(UIText.GitPrivacyPage_CopyCurrentKeyButton_Text);
		copyCurrentKeyButton.setLayoutData(buttonLayoutData);
		copyCurrentKeyButton.addSelectionListener(widgetSelectedAdapter(e -> {
			Optional<String> keyOptional = managesKeyStorage.readCurrentKey();
			keyOptional.ifPresent(key -> {
				TextTransfer textTransfer = TextTransfer.getInstance();
				clipboard.setContents(new Object[] { key },
						new Transfer[] { textTransfer });
			});
		}));
		copyCurrentKeyButton
				.setEnabled(managesKeyStorage.readCurrentKey().isPresent());
		managesKeyStorage.addKeySavedListener(
				() -> copyCurrentKeyButton.setEnabled(true));

		IPreferenceStore preferenceStore = Activator.getDefault()
				.getPreferenceStore();
		Button migrateButton = new Button(composite, SWT.NONE);
		migrateButton.setText(UIText.GitPrivacyPage_MigrateFromPasswordButton_Text);
		migrateButton.setLayoutData(buttonLayoutData);
		migrateButton.addSelectionListener(widgetSelectedAdapter(e -> {
			passwordToKeyMigration.migrate(config, managesKeyStorage,
					preferenceStore);
		}));
		migrateButton.setEnabled(
				passwordToKeyMigration.canMigrate(config, preferenceStore));

		Button openKeyDirectoryButton = new Button(composite, SWT.NONE);
		openKeyDirectoryButton.setText(UIText.GitPrivacyPage_OpenKeyDirectoryButton_Text);
		openKeyDirectoryButton.setLayoutData(buttonLayoutData);
		openKeyDirectoryButton
				.addSelectionListener(widgetSelectedAdapter(e -> {
					File keysDirectory = managesKeyStorage.getKeysDirectory();
					keysDirectory.mkdirs();
					Program.launch(
							keysDirectory
							.getPath());
				}));

		// Button newMigrateButton = new Button(composite, SWT.NONE);
		// newMigrateButton
		// .setText(UIText.GitPrivacyPage_MigrateFromPasswordButton_Text);
		// newMigrateButton.setLayoutData(buttonLayoutData);
		// newMigrateButton.addSelectionListener(widgetSelectedAdapter((e) -> {
		// WizardDialog dialog = new WizardDialog(composite.getShell(),
		// new PasswordMigrationWizard());
		// dialog.open();
		// }));

		return composite;
	}


	@Override
	public void dispose() {
		if (clipboard != null) {
			clipboard.dispose();
			clipboard = null;
		}

		if (migrateFromGitConfigConfirmationDialog != null) {
			migrateFromGitConfigConfirmationDialog.close();
			migrateFromGitConfigConfirmationDialog = null;
		}

		if (archiveKeyConfirmationDialog != null) {
			archiveKeyConfirmationDialog.close();
			archiveKeyConfirmationDialog = null;
		}

		if (eclipseDialog != null) {
			eclipseDialog.close();
			eclipseDialog = null;
		}

		if (eclipseOrGitDialog != null) {
			eclipseOrGitDialog.close();
			eclipseOrGitDialog = null;
		}

		super.dispose();
	}

}
