package de.empri.devops.gitprivacy.preferences.shared;

import org.eclipse.jgit.lib.Config;

import de.empri.devops.gitprivacy.preferences.UIPreferences;

public class GitPrivacyConfigReader {

	public String getPassword(Config config) {
		return config.getString(UIPreferences.GitPrivacyPage_GitConfigPrivacySection, null,
				UIPreferences.GitPrivacyPage_GitConfigPrivacySection_Password);
	}

	public String getSalt(Config config) {
		return config.getString(UIPreferences.GitPrivacyPage_GitConfigPrivacySection, null,
				UIPreferences.GitPrivacyPage_GitConfigPrivacySection_Salt);
	}

}
