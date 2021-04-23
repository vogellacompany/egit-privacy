package de.empri.devops.gitprivacy.hook;

import org.eclipse.jgit.lib.Config;

/**
 *
 */
public class ParsesGitPrivacyPattern {

	private boolean modifyCommitMonth;

	private boolean modifyCommitDay;

	private boolean modifyCommitHour;

	private boolean modifyCommitMinute;

	private boolean modifyCommitSecond;

	private boolean modifyCommitDate;

	/**
	 * @param config
	 */
	public ParsesGitPrivacyPattern(Config config) {
		String pattern = config.getString("privacy", null, "pattern"); //$NON-NLS-1$//$NON-NLS-2$
		if (pattern == null) {
			return;
		}
		for (int i = 0; i < pattern.length(); i++) {
			parse(pattern.charAt(i));
		}
	}

	private void parse(char character) {
		switch (character) {
		case 'M':
			modifyCommitMonth = true;
			modifyCommitDate = true;
			break;
		case 'd':
			modifyCommitDay = true;
			modifyCommitDate = true;
			break;
		case 'h':
			modifyCommitHour = true;
			modifyCommitDate = true;
			break;
		case 'm':
			modifyCommitMinute = true;
			modifyCommitDate = true;
			break;
		case 's':
			modifyCommitSecond = true;
			modifyCommitDate = true;
			break;

		default:
			break;
		}
	}

	/**
	 * @return modifyCommitMonth
	 */
	public boolean isModifyCommitMonth() {
		return modifyCommitMonth;
	}

	/**
	 * @return modifyCommitDay
	 */
	public boolean isModifyCommitDay() {
		return modifyCommitDay;
	}

	/**
	 * @return modifyCommitHour
	 */
	public boolean isModifyCommitHour() {
		return modifyCommitHour;
	}

	/**
	 * @return modifyCommitMinute
	 */
	public boolean isModifyCommitMinute() {
		return modifyCommitMinute;
	}

	/**
	 * @return modifyCommitSecond
	 */
	public boolean isModifyCommitSecond() {
		return modifyCommitSecond;
	}

	/**
	 * @return modifyCommitDate
	 */
	public boolean isModifyCommitDate() {
		return modifyCommitDate;
	}

}
