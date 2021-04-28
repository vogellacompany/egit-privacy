package de.empri.devops.gitprivacy.preferences.shared;

import java.util.List;
import java.util.Optional;

import org.eclipse.jgit.lib.Repository;

public class OriginalCommitDateEncoderFactory {

	/**
	 * Returns an {@link OriginalCommitDateEncoder} if the repository contains at
	 * least one encryption key. Otherwise the {@link Optional} will be empty.
	 * 
	 * @param repository that the encoder is supposed to work on
	 * @return encoder or empty if the repository doesn't contain encryption keys
	 */
	public static Optional<OriginalCommitDateEncoder> build(Repository repository) {
		List<String> allKeys = new ManagesKeyStorage(repository.getDirectory()).readAllKeys();
		if (!allKeys.isEmpty()) {
			return Optional.of(new OriginalCommitDateEncoder(new Crypto(allKeys)));
		} else {
			return Optional.empty();
		}
	}

}
