package de.empri.devops.gitprivacy.preferences;

import org.eclipse.osgi.util.NLS;

public class UIText extends NLS {

	private static final String BUNDLE_NAME = "de.empri.devops.gitprivacy.preferences.uitext";

	public static String GitPrivacyPage_OpenKeyDirectoryButton_Text;

	public static String GitPrivacyPage_GenerateKeyButton_Text;

	public static String GitPrivacyPage_ReplaceKeyButton_Text;

	public static String GitPrivacyPage_CopyCurrentKeyButton_Text;

	public static String GitPrivacyPage_MigrateFromPasswordButton_Text;


	static {
		initializeMessages(BUNDLE_NAME, UIText.class);
	}
}
