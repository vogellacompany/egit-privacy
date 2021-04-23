package de.empri.devops.gitprivacy.hook;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.eclipse.jgit.lib.Config;

/**
 * Provides the commit date for a commit.
 *
 */
public class ProvidesCommitDate {

	/**
	 * Default constructor
	 *
	 */
	public ProvidesCommitDate() {
	}

	/**
	 * @param gitConfig
	 * @return Dates to be used for a commit. Potentially provides a redated
	 *         commit date according to the preferences from
	 *         {@link PrivacyPreferencePage}. If the commit date gets redated,
	 *         the {@link CommitDateResult} will contain the original date so it
	 *         can be saved in an encrypted form.
	 */
	public CommitDateResult commitDate(Config gitConfig) {
		LocalDateTime commitDate = LocalDateTime.now();
		LocalDateTime originalCommitDate = commitDate;
		int year = commitDate.getYear();
		int month = commitDate.getMonthValue();
		int dayOfMonth = commitDate.getDayOfMonth();
		int hour = commitDate.getHour();
		int minute = commitDate.getMinute();
		int second = commitDate.getSecond();

		ParsesGitPrivacyPattern patternConfig = new ParsesGitPrivacyPattern(
				gitConfig);
		if (patternConfig.isModifyCommitDate()) {

			if (patternConfig.isModifyCommitMonth()) {
				month = 1;
			}
			if (patternConfig.isModifyCommitDay()) {
				dayOfMonth = 1;
			}
			if (patternConfig.isModifyCommitHour()) {
				hour = 0;
			}
			if (patternConfig.isModifyCommitMinute()) {
				minute = 0;
			}
			if (patternConfig.isModifyCommitSecond()) {
				second = 0;
			}

			commitDate = LocalDateTime.of(year, month, dayOfMonth, hour, minute,
					second);
		}

		ParsesGitPrivacyLimit limitConfig = new ParsesGitPrivacyLimit(
				gitConfig);
		if (limitConfig.isLimitCommitTime()) {
			int lowerLimit = limitConfig.getLowerLimit();
			LocalDateTime lowerLimitDate = LocalDateTime.of(year, month, dayOfMonth, lowerLimit, 0,
					0);
			int upperLimit = limitConfig.getUpperLimit();
			LocalDateTime upperLimitDate = LocalDateTime.of(year, month, dayOfMonth, upperLimit, 0,
					0);
			if (commitDate.isBefore(lowerLimitDate)) {
				commitDate = lowerLimitDate;
			} else if (commitDate.isAfter(upperLimitDate)) {
				commitDate = upperLimitDate;
			}
		}

		return new CommitDateResult(Date.from(originalCommitDate.atZone(ZoneId.systemDefault()).toInstant()),
				Date.from(commitDate.atZone(ZoneId.systemDefault()).toInstant()));
	}

	/**
	 * Value class that holds the commit date for a commit. If the privacy
	 * plugin changes the commit date it also holds the original commit date.
	 *
	 */
	public class CommitDateResult {

		private final Date originalCommitDate;

		private final Date commitDate;

		/**
		 * @param originalCommitDate
		 *            original commit date, will be equal to the commit date, if
		 *            the commit date hasn't been redated
		 * @param redactedCommitDate
		 *            commit date, possibly redated by the privacy plugin
		 */
		private CommitDateResult(
				Date originalCommitDate,
				Date redactedCommitDate) {
			this.originalCommitDate = originalCommitDate;
			this.commitDate = redactedCommitDate;
		}

		/**
		 * @return original commit date, will be equal to the commit date, if it
		 *         hasn't been redated
		 */
		public Date getOriginalCommitDate() {
			return originalCommitDate;
		}

		/**
		 * @return commit date, possibly redated by the privacy plugin
		 */
		public Date getCommitDate() {
			return commitDate;
		}

		/**
		 * @return if the commit date has been redated
		 */
		public boolean isRedated() {
			return !originalCommitDate.equals(getCommitDate());
		}

	}

}
