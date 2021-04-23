package de.empri.devops.gitprivacy.preferences.shared;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Optional;

/**
 *
 */
public class ManagesKeyStorage {

	public interface KeySavedListener extends EventListener {

		void handleEvent();

	}

	private File keysDirectory;

	private File currentKeyFile;

	private File keysArchiveDirectory;

	private List<KeySavedListener> keySavedListeners = new ArrayList<>();

	/**
	 * @param gitDirectory
	 */
	public ManagesKeyStorage(File gitDirectory) {
		keysDirectory = new File(gitDirectory, "keys"); //$NON-NLS-1$
		currentKeyFile = new File(keysDirectory, "current"); //$NON-NLS-1$
		keysArchiveDirectory = new File(keysDirectory, "archive"); //$NON-NLS-1$
	}

	/**
	 * @return
	 */
	public Optional<String> readCurrentKey() {
		return read(currentKeyFile);
	}

	private Optional<String> read(File file) {
		byte[] encoded;
		try {
			encoded = Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			return Optional.empty();
		}
		return Optional.ofNullable(new String(encoded, StandardCharsets.UTF_8));
	}

	public List<String> readAllKeys() {
		List<String> allKeys = new ArrayList<>();
		Optional<String> currentKeyOptional = readCurrentKey();
		if (currentKeyOptional.isPresent()) {
			allKeys.add(currentKeyOptional.get());

			File[] listFiles = keysArchiveDirectory.listFiles();
			if (listFiles != null) {
				for (File keyFile : listFiles) {
					Optional<String> keyOptional = read(keyFile);
					if (keyOptional.isPresent()) {
						allKeys.add(keyOptional.get());
					}
				}
			}
		}
		return allKeys;
	}

	public void store(String generateKey)
			throws FileNotFoundException, IOException {
		store(generateKey, true);
	}

	public void store(String key, boolean archiveCurrentKey)
			throws IOException {
		// TODO(FAP): find out the proper way to save files in EGit
		if (!currentKeyFile.exists()) {
			keysDirectory.mkdirs();
			write(key, currentKeyFile);
		} else {
			if (archiveCurrentKey) {
				archiveCurrentKey();
			}
			currentKeyFile.delete();
			write(key, currentKeyFile);
		}
	}

	private String archiveCurrentKey()
			throws FileNotFoundException, IOException {
		keysArchiveDirectory.mkdirs();
		int highestNumber = findHighestKeyNumber();
		File archiveFile = new File(keysArchiveDirectory,
				String.valueOf(highestNumber + 1));
		String currentKey = readCurrentKey().get();
		write(currentKey, archiveFile);
		return currentKey;
	}

	private void write(String key, File keyFile)
			throws FileNotFoundException, IOException {
		try (OutputStreamWriter writer = new OutputStreamWriter(
				new FileOutputStream(keyFile), StandardCharsets.UTF_8)) {
			writer.write(key);
			keySavedListeners.forEach(KeySavedListener::handleEvent);
		}
	}

	private int findHighestKeyNumber() {
		int highestNumber = 0;
		for (File file : keysArchiveDirectory.listFiles()) {
			int fileName = Integer.parseInt(file.getName());
			if (fileName > highestNumber) {
				highestNumber = fileName;
			}
		}
		return highestNumber;
	}

	public File getKeysDirectory() {
		return keysDirectory;
	}

	public void addKeySavedListener(KeySavedListener listener) {
		keySavedListeners.add(listener);
	}

	public void removeKeySavedListener(KeySavedListener listener) {
		keySavedListeners.remove(listener);
	}

}
