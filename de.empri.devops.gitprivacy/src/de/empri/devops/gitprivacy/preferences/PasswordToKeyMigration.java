package de.empri.devops.gitprivacy.preferences;

import java.io.IOException;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jgit.lib.Config;
import org.eclipse.swt.widgets.Shell;

import de.empri.devops.gitprivacy.preferences.shared.GitPrivacyConfigReader;
import de.empri.devops.gitprivacy.preferences.shared.ManagesKeyStorage;

public class PasswordToKeyMigration {

	private Shell shell;

	private GitPrivacyConfigReader configReader;

	public PasswordToKeyMigration(Shell shell) {
		this.shell = shell;
		configReader = new GitPrivacyConfigReader();
	}

	public boolean canMigrate(Config config,
			IPreferenceStore preferenceStore) {
		boolean eclipsePreferenceMigrationPossible = eclipsePreferenceMigrationPossible(
				preferenceStore);

		boolean gitConfigMigrationPossible = gitConfigMigrationPossible(config);

		return eclipsePreferenceMigrationPossible || gitConfigMigrationPossible;
	}

	private boolean gitConfigMigrationPossible(Config config) {
		return configReader.getPassword(config) != null
				&& configReader.getSalt(config) != null;
	}

	private boolean eclipsePreferenceMigrationPossible(
			IPreferenceStore preferenceStore) {
//		String password = preferenceStore
//				.getString(UIPreferences.GIT_PRIVACY_PASSWORD);
//		String salt = preferenceStore
//				.getString(UIPreferences.GIT_PRIVACY_PASSWORD_SALT);
//		return !password.isEmpty() && !salt.isEmpty();
		return true;
	}

	public void migrate(Config config, ManagesKeyStorage managesKeyStorage,
			IPreferenceStore preferenceStore) {
//		boolean eclipsePreferenceMigrationPossible = eclipsePreferenceMigrationPossible(
//				preferenceStore);
//		boolean gitConfigMigrationPossible = gitConfigMigrationPossible(config);
//
//		boolean useFromEclipsePreferences = false;
//		boolean useFromGitConfig = false;
//		if (eclipsePreferenceMigrationPossible && gitConfigMigrationPossible) {
//			MessageDialog eclipseOrGitDialog = new MessageDialog(shell, // dialog
//					UIText.GitPrivacyPage_PwFromEclipsePrefsDialog_Title, // title
//					null, // use default window icon
//					UIText.PasswordToKeyMigration_PwFromEclipsePrefsOrGitConfigDialog_Message,
//					MessageDialog.QUESTION, 0,
//					UIText.GitPrivacyPage_PwFromEclipsePrefsDialog_UseButton,
//					UIText.GitPrivacyPage_PwFromEclipsePrefsDialog_DontUseButton,
//					UIText.GitPrivacyPage_PwFromGitConfigDialog_UseButton);
//			eclipseOrGitDialog.setBlockOnOpen(true);
//			eclipseOrGitDialog.open();
//			useFromEclipsePreferences = (eclipseOrGitDialog
//					.getReturnCode() == 0);
//			useFromGitConfig = (eclipseOrGitDialog.getReturnCode() == 2);
//		} else if (eclipsePreferenceMigrationPossible) {
//			MessageDialog eclipseDialog = new MessageDialog(shell, // dialog
//					UIText.GitPrivacyPage_PwFromEclipsePrefsDialog_Title, // title
//					null, // use default window icon
//					UIText.GitPrivacyPage_PwFromEclipsePrefsDialog_Message,
//					MessageDialog.QUESTION, 0,
//					UIText.GitPrivacyPage_PwFromEclipsePrefsDialog_UseButton,
//					UIText.GitPrivacyPage_PwFromEclipsePrefsDialog_DontUseButton);
//			eclipseDialog.setBlockOnOpen(true);
//			eclipseDialog.open();
//			useFromEclipsePreferences = (eclipseDialog.getReturnCode() == 0);
//		} else if (gitConfigMigrationPossible) {
//			MessageDialog migrateFromGitConfigConfirmationDialog = new MessageDialog(
//					shell,
//					UIText.GitPrivacyPage_PwFromGitConfigDialog_Title, // dialog
//					// title
//					null, // use default window icon
//					UIText.GitPrivacyPage_PwFromGitConfigDialog_Message,
//					MessageDialog.QUESTION, 0,
//					UIText.GitPrivacyPage_PwFromGitConfigDialog_UseButton,
//					UIText.GitPrivacyPage_PwFromGitConfigDialog_DontUseButton);
//			migrateFromGitConfigConfirmationDialog.setBlockOnOpen(true);
//			migrateFromGitConfigConfirmationDialog.open();
//			useFromGitConfig = (migrateFromGitConfigConfirmationDialog
//					.getReturnCode() == 0);
//		}
//
//		if (useFromEclipsePreferences) {
//			String key = Crypto.deriveKey(
//					preferenceStore
//							.getString(UIPreferences.GIT_PRIVACY_PASSWORD),
//					preferenceStore.getString(
//							UIPreferences.GIT_PRIVACY_PASSWORD_SALT));
//			try {
//				replaceKey(managesKeyStorage, key);
//				// TODO(FAP): remove pw&salt from preferences?
//				return;
//			} catch (IOException e1) {
//				Activator.handleError(e1.getMessage(), e1, true);
//			}
//		} else if (useFromGitConfig) {
//			String key = Crypto.deriveKey(configReader.getPassword(config),
//					configReader.getSalt(config));
//			try {
//				replaceKey(managesKeyStorage, key);
//				// TODO(FAP): comment out (like python version does)
//				// pw&salt from git config? I think we can only delete
//				return;
//			} catch (IOException e1) {
//				Activator.handleError(e1.getMessage(), e1, true);
//			}
//		}
	}

	public void replaceKey(ManagesKeyStorage managesKeyStorage, String key)
			throws IOException {
//		boolean archiveCurrentKey = false;
//		if (managesKeyStorage.readCurrentKey().isPresent()) {
//			int confirmButtonId = 0;
//			MessageDialog archiveKeyConfirmationDialog = new MessageDialog(
//					shell,
//					UIText.GitPrivacyPage_ArchiveKeyDialog_Title, null,
//					UIText.GitPrivacyPage_ArchiveKeyDialog_Message,
//					MessageDialog.QUESTION, confirmButtonId,
//					UIText.GitPrivacyPage_ArchiveKeyDialog_ArchiveButton,
//					UIText.GitPrivacyPage_ArchiveKeyDialog_CancelButton,
//					UIText.GitPrivacyPage_ArchiveKeyDialog_DontArchiveButton);
//			archiveKeyConfirmationDialog.setBlockOnOpen(true);
//			archiveKeyConfirmationDialog.open();
//			int returnCode = archiveKeyConfirmationDialog.getReturnCode();
//			boolean cancel = (returnCode == -1 || returnCode == 1);
//			if (cancel) {
//				return;
//			}
//
//			archiveCurrentKey = (returnCode == confirmButtonId);
//		}
//
//		managesKeyStorage.store(key, archiveCurrentKey);
//
//		MessageDialog.openInformation(shell,
//				UIText.GitPrivacyPage_KeySaved_Title,
//				UIText.GitPrivacyPage_KeySaved_Message);
	}

}
