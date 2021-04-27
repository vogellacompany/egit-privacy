package de.empri.devops.gitprivacy.preferences.shared;

import java.io.BufferedReader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Encodes and decodes the original commit date to a commit message.
 *
 */
public class OriginalCommitDateEncoder {

	private static final String TIMEZONE_FORMAT_PATTERN = "Z";

	private static final String PREFIX = "GitPrivacy: ";

	private static final Pattern pattern = Pattern.compile("(?m)^" + PREFIX + "(\\S+)(?: (\\S+))?"); //$NON-NLS-2$

	private static final SimpleDateFormat format = new SimpleDateFormat(TIMEZONE_FORMAT_PATTERN);

	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TIMEZONE_FORMAT_PATTERN);

	private Crypto crypto;

	private ManagesKeyStorage managesKeyStorage;

	/**
	 * Initializes the crypto class for encoding and decoding. Adds a listener to
	 * password and salt so the crypto class gets rebuilt when they change.
	 *
	 * @param gitDirectory
	 */
	public OriginalCommitDateEncoder(Crypto crypto) {
		this.crypto = crypto;
	}

	/**
	 * @param commitMessage      message of a commit
	 * @param originalCommitDate original commit date to be added in an encrypted
	 *                           form to the commit message
	 * @return commit message with the original commit date added in an encrypted
	 *         form
	 */
	public String encode(String commitMessage, Date originalCommitDate) {
		Optional<DecodedDates> dates = decode(commitMessage);
		Optional<ZonedDateTime> authoredDateOptional = Optional.empty();
		List<String> commitMessageLines = removeEncodedMessage(commitMessage);
		if (dates.isPresent()) {
			authoredDateOptional = Optional.of(dates.get().getAuthoredDateTime());
		}
		return addEncodedOriginalCommitDate(commitMessageLines, originalCommitDate, authoredDateOptional);
	}

	private List<String> removeEncodedMessage(String commitMessage) {
		return new BufferedReader(new StringReader(commitMessage)).lines().filter(line -> !line.startsWith(PREFIX))
				.collect(Collectors.toList());
	}

	private String addEncodedOriginalCommitDate(List<String> commitMessageLines, Date originalCommitDate,
			Optional<ZonedDateTime> authoredDateOptional) {
		String commitedDate = toRawDateFormat(originalCommitDate);
		String authoredDate;
		if (authoredDateOptional.isPresent()) {
			authoredDate = toRawDateFormat(authoredDateOptional.get());
		} else {
			authoredDate = commitedDate;
		}
		String encryptedDates = crypto.encrypt(authoredDate) + " "
				+ crypto.encrypt(commitedDate);
		String lastLine = commitMessageLines.get(commitMessageLines.size() - 1);
		if (!lastLine.isEmpty()) {
			commitMessageLines.add("");
		}
		commitMessageLines.add(PREFIX + encryptedDates);
		return commitMessageLines.stream().collect(Collectors.joining("\n"));
	}

	/**
	 * @param originalCommitDate
	 * @return Seconds sine 1970 in UTC plus time zone
	 */
	private String toRawDateFormat(Date originalCommitDate) {
		String timeZone = format.format(originalCommitDate);
		String secondsSince1970UTC = String.valueOf(originalCommitDate.getTime() / 1000);
		return secondsSince1970UTC + " "
				+ timeZone;
	}

	/**
	 * @param authoredDateTime
	 * @return Seconds sine 1970 in UTC plus time zone
	 */
	private String toRawDateFormat(ZonedDateTime authoredDateTime) {
		String timezone = authoredDateTime.format(formatter);
		return authoredDateTime.toEpochSecond() + " " + timezone;
	}

	/**
	 * Extracts the encrypted original commit date from a commit message.
	 *
	 * @param commitMessage
	 * @return the decrypted original commit date
	 */
	public Optional<DecodedDates> decode(String commitMessage) {
		// TODO(FAP): add downwards compatible way to support / decode new
		// encoding format
		Matcher matcher = pattern.matcher(commitMessage);
		if (matcher.find()) {
			String encodedAuthoredDate = matcher.group(1);
			String decryptedAuthoredDate = decryptIfNotNull(encodedAuthoredDate);
			if (decryptedAuthoredDate.isEmpty()) {
				return Optional.empty();
			}

			String encodedCommitDate = matcher.group(2);
			String decryptedCommittedDate = decryptIfNotNull(encodedCommitDate);
			if (decryptedCommittedDate.isEmpty()) {
				String[] dates = decryptedAuthoredDate.split(";");
				decryptedAuthoredDate = dates[0];
				decryptedCommittedDate = dates[1];
			}
			return Optional.of(toDate(decryptedAuthoredDate, decryptedCommittedDate));
		}
		return Optional.empty();
	}

	private String decryptIfNotNull(String encodedDate) {
		return encodedDate != null ? crypto.decrypt(encodedDate) : "";
	}

	private DecodedDates toDate(String decryptedAuthoredDate, String decryptedCommittedDate) {
		String[] authored = decryptedAuthoredDate.split(" ");
		String[] committed = decryptedCommittedDate.split(" ");
		ZonedDateTime authoredDateTime = zonedDateTime(authored);
		ZonedDateTime committedDateTime = zonedDateTime(committed);
		return new DecodedDates(authoredDateTime, committedDateTime);
	}

	private ZonedDateTime zonedDateTime(String[] authored) {
		Instant instant = Instant.ofEpochSecond(Long.valueOf(authored[0]).longValue());
		ZoneOffset zoneOffset = ZoneOffset.of(authored[1]);
		return ZonedDateTime.ofInstant(instant, zoneOffset);
	}

	/**
	 * Holds the original authored date and committed date of a commit.
	 */
	public class DecodedDates {

		private ZonedDateTime authoredDateTime;

		private ZonedDateTime committedDateTime;

		/**
		 * @param authoredDateTime
		 * @param committedDateTime
		 */
		protected DecodedDates(ZonedDateTime authoredDateTime, ZonedDateTime committedDateTime) {
			super();
			this.authoredDateTime = authoredDateTime;
			this.committedDateTime = committedDateTime;
		}

		/**
		 * @return authored date time
		 */
		public ZonedDateTime getAuthoredDateTime() {
			return authoredDateTime;
		}

		/**
		 * @return committed
		 */
		public ZonedDateTime getCommittedDateTime() {
			return committedDateTime;
		}

	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

}
