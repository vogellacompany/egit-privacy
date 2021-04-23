package de.empri.devops.gitprivacy.preferences.shared;

import org.eclipse.jgit.lib.Config;

public class GitPrivacyConfigReader {

	public String getPassword(Config config) {
		return config.getString("privacy",
				null, "password");
	}

	public String getSalt(Config config) {
		return config.getString("privacy", null, "salt");
	}

}
