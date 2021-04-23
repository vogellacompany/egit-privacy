package de.empri.devops.gitprivacy.hook;

import org.eclipse.jgit.lib.Config;

/**
 *
 */
public class ParsesGitPrivacyLimit {

	private Integer lowerLimit;

	private Integer upperLimit;

	private boolean limitCommitTime;

	/**
	 * @param gitConfig
	 */
	public ParsesGitPrivacyLimit(Config gitConfig) {
		String limit = gitConfig.getString("privacy", null, "pattern"); //$NON-NLS-1$//$NON-NLS-2$
		if (limit == null) {
			return;
		}
		String[] limits = limit.split("-"); //$NON-NLS-1$
		limitCommitTime = true;
		try {
			lowerLimit = Integer.valueOf(limits[0]);
			upperLimit = Integer.valueOf(limits[1]);
		} catch (NumberFormatException ex) {
			// do nothing on purpose
		}

		if (lowerLimit == null || upperLimit == null
				|| lowerLimitLargerThanUpperLimit()) {
			limitCommitTime = false;
		}
	}

	/**
	 * @return lowerLimit
	 */
	public int getLowerLimit() {
		return lowerLimit.intValue();
	}

	/**
	 * @return upperLimit
	 */
	public int getUpperLimit() {
		return upperLimit.intValue();
	}

	/**
	 * @return limitCommitTime
	 */
	public boolean isLimitCommitTime() {
		return limitCommitTime;
	}

	private boolean lowerLimitLargerThanUpperLimit() {
		return lowerLimit.compareTo(upperLimit) > 0;
	}

}
