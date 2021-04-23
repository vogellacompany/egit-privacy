//package de.empri.devops.gitprivacy.hook;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.StringReader;
//import java.text.SimpleDateFormat;
//import java.time.Instant;
//import java.time.ZoneOffset;
//import java.time.ZonedDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.Date;
//import java.util.List;
//import java.util.Optional;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//import java.util.stream.Collectors;
//
//import org.eclipse.jface.preference.IPreferenceStore;
//
//import de.empri.devops.gitprivacy.hook.shared.Crypto;
//import de.empri.devops.gitprivacy.hook.shared.ManagesKeyStorage;
//
///**
// * Encodes and decodes the original commit date to a commit message.
// *
// */
//public class OriginalCommitDateEncoder {
//
//	private static final String TIMEZONE_FORMAT_PATTERN = "Z"; //$NON-NLS-1$
//
//	private static final String PREFIX = "GitPrivacy: "; //$NON-NLS-1$
//
//	private static final Pattern pattern = Pattern
//			.compile("(?m)^" + PREFIX + "(\\S+)(?: (\\S+))?"); //$NON-NLS-1$ //$NON-NLS-2$
//
//	private static final SimpleDateFormat format = new SimpleDateFormat(
//			TIMEZONE_FORMAT_PATTERN);
//
//	private static final DateTimeFormatter formatter = DateTimeFormatter
//			.ofPattern(TIMEZONE_FORMAT_PATTERN);
//
//	private IPreferenceStore preferenceStore;
//
//	private Crypto crypto;
//
//	private ManagesKeyStorage managesKeyStorage;
//
//	private Optional<String> key;
//
//	/**
//	 * Initializes the crypto class for encoding and decoding. Adds a listener
//	 * to password and salt so the crypto class gets rebuilt when they change.
//	 *
//	 * @param gitDirectory
//	 */
//	public OriginalCommitDateEncoder(File gitDirectory) {
//		managesKeyStorage = new ManagesKeyStorage(gitDirectory);
//
//		// TODO(FAP): should load keys also when somethings changes
//		// easy way would be to just reload all keys for encode() and decode()
//		// do we even need the constructor then?
//		// maybe logic for migration check could go somewhere else?
//		List<String> allKeys = managesKeyStorage.readAllKeys();
////		String password = preferenceStore
////				.getString(UIPreferences.GIT_PRIVACY_PASSWORD);
//		if (!allKeys.isEmpty()) {
//			crypto = new Crypto(allKeys);
//			// TODO(FAP): add change listeners to ManagesKeyStorage (should be a
//			// Singleton then?)
//			// Just read the key for each enc/dec action? group actions for mass
//			// actions, if we support them later on
//			// DOESNT MATTER: gets newly instantiated before every commit anyways
//			// } else if (!password.isEmpty()) {
//			// TODO(FAP): show dialog and offer conversion to key?
//			// 1. no current key exists -> check if pw&salt are in preferences
//			// or git config
//			// 2. offer conversion or offer to open the repository config page (better for code reuse?)
////			crypto = new Crypto(
////					preferenceStore
////							.getString(UIPreferences.GIT_PRIVACY_PASSWORD),
////					preferenceStore.getString(
////							UIPreferences.GIT_PRIVACY_PASSWORD_SALT));
////			preferenceStore.addPropertyChangeListener(e -> {
////				if (UIPreferences.GIT_PRIVACY_PASSWORD
////						.equals(e.getProperty())) {
////					crypto = new Crypto((String) e.getNewValue(),
////							preferenceStore.getString(
////									UIPreferences.GIT_PRIVACY_PASSWORD_SALT));
////				} else if (UIPreferences.GIT_PRIVACY_PASSWORD_SALT
////						.equals(e.getProperty())) {
////					crypto = new Crypto(
////							preferenceStore.getString(
////									UIPreferences.GIT_PRIVACY_PASSWORD),
////							(String) e.getNewValue());
////				}
//// });
//		} else {
//			crypto = new Crypto();
//		}
//
//	}
//
//	/**
//	 * @param commitMessage
//	 *            message of a commit
//	 * @param originalCommitDate
//	 *            original commit date to be added in an encrypted form to the
//	 *            commit message
//	 * @return commit message with the original commit date added in an
//	 *         encrypted form
//	 */
//	public String encode(String commitMessage,
//			Date originalCommitDate) {
//		Optional<DecodedDates> dates = decode(commitMessage);
//		Optional<ZonedDateTime> authoredDateOptional = Optional.empty();
//		List<String> commitMessageLines = removeEncodedMessage(commitMessage);
//		if (dates.isPresent()) {
//			authoredDateOptional = Optional
//					.of(dates.get().getAuthoredDateTime());
//		}
//		return addEncodedOriginalCommitDate(commitMessageLines,
//				originalCommitDate,
//				authoredDateOptional);
//	}
//
//	private List<String> removeEncodedMessage(String commitMessage) {
//		return new BufferedReader(new StringReader(commitMessage))
//				.lines()
//				.filter(line -> !line.startsWith(PREFIX))
//				.collect(Collectors.toList());
//	}
//
//	private String addEncodedOriginalCommitDate(List<String> commitMessageLines,
//			Date originalCommitDate,
//			Optional<ZonedDateTime> authoredDateOptional) {
//		String commitedDate = toRawDateFormat(originalCommitDate);
//		String authoredDate;
//		if (authoredDateOptional.isPresent()) {
//			authoredDate = toRawDateFormat(authoredDateOptional.get());
//		} else {
//			authoredDate = commitedDate;
//		}
//		String encryptedDates = crypto.encrypt(authoredDate) + " " //$NON-NLS-1$
//				+ crypto.encrypt(commitedDate);
//		String lastLine = commitMessageLines.get(commitMessageLines.size() - 1);
//		if (!lastLine.isEmpty()) {
//			commitMessageLines.add(""); //$NON-NLS-1$
//		}
//		commitMessageLines.add(PREFIX + encryptedDates);
//		return commitMessageLines.stream().collect(Collectors.joining("\n")); //$NON-NLS-1$
//	}
//
//	/**
//	 * @param originalCommitDate
//	 * @return Seconds sine 1970 in UTC plus time zone
//	 */
//	private String toRawDateFormat(Date originalCommitDate) {
//		String timeZone = format.format(originalCommitDate);
//		String secondsSince1970UTC = String
//				.valueOf(originalCommitDate.getTime() / 1000);
//		return secondsSince1970UTC
//				+ " " //$NON-NLS-1$
//				+ timeZone;
//	}
//
//	/**
//	 * @param authoredDateTime
//	 * @return Seconds sine 1970 in UTC plus time zone
//	 */
//	private String toRawDateFormat(ZonedDateTime authoredDateTime) {
//		String timezone = authoredDateTime.format(formatter);
//		return authoredDateTime.toEpochSecond() + " " + timezone; //$NON-NLS-1$
//	}
//
//	/**
//	 * Extracts the encrypted original commit date from a commit message.
//	 *
//	 * @param commitMessage
//	 * @return the decrypted original commit date
//	 */
//	public Optional<DecodedDates> decode(String commitMessage) {
//		// TODO(FAP): add downwards compatible way to support / decode new
//		// encoding format
//		Matcher matcher = pattern.matcher(commitMessage);
//		if (matcher.find()) {
//			String encodedAuthoredDate = matcher.group(1);
//			String decryptedAuthoredDate = decryptIfNotNull(encodedAuthoredDate);
//			if (decryptedAuthoredDate.isEmpty()) {
//				return Optional.empty();
//			}
//
//			String encodedCommitDate = matcher.group(2);
//			String decryptedCommittedDate = decryptIfNotNull(encodedCommitDate);
//			if (decryptedCommittedDate.isEmpty()) {
//				String[] dates = decryptedAuthoredDate.split(";"); //$NON-NLS-1$
//				decryptedAuthoredDate = dates[0];
//				decryptedCommittedDate = dates[1];
//			}
//			return Optional
//					.of(toDate(decryptedAuthoredDate, decryptedCommittedDate));
//		}
//		return Optional.empty();
//	}
//
//	private String decryptIfNotNull(String encodedDate) {
//		return encodedDate != null
//				? crypto.decrypt(encodedDate)
//				: ""; //$NON-NLS-1$
//	}
//
//	private DecodedDates toDate(String decryptedAuthoredDate,
//			String decryptedCommittedDate) {
//		String[] authored = decryptedAuthoredDate.split(" "); //$NON-NLS-1$
//		String[] committed = decryptedCommittedDate.split(" "); //$NON-NLS-1$
//		ZonedDateTime authoredDateTime = zonedDateTime(authored);
//		ZonedDateTime committedDateTime = zonedDateTime(committed);
//		return new DecodedDates(authoredDateTime, committedDateTime);
//	}
//
//	private ZonedDateTime zonedDateTime(String[] authored) {
//		Instant instant = Instant
//				.ofEpochSecond(Long.valueOf(authored[0]).longValue());
//		ZoneOffset zoneOffset = ZoneOffset.of(authored[1]);
//		return ZonedDateTime.ofInstant(instant, zoneOffset);
//	}
//
//	/**
//	 * Holds the original authored date and committed date of a commit.
//	 */
//	public class DecodedDates {
//
//		private ZonedDateTime authoredDateTime;
//
//		private ZonedDateTime committedDateTime;
//
//		/**
//		 * @param authoredDateTime
//		 * @param committedDateTime
//		 */
//		protected DecodedDates(
//				ZonedDateTime authoredDateTime,
//				ZonedDateTime committedDateTime) {
//			super();
//			this.authoredDateTime = authoredDateTime;
//			this.committedDateTime = committedDateTime;
//		}
//
//		/**
//		 * @return authored date time
//		 */
//		public ZonedDateTime getAuthoredDateTime() {
//			return authoredDateTime;
//		}
//
//		/**
//		 * @return committed
//		 */
//		public ZonedDateTime getCommittedDateTime() {
//			return committedDateTime;
//		}
//
//	}
//
//	public void dispose() {
//		// TODO Auto-generated method stub
//
//	}
//
//}
