package de.empri.devops.gitprivacy.preferences;

import java.util.Optional;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.egit.core.internal.IRepositoryCommit;
import org.eclipse.jgit.revwalk.RevCommit;

import de.empri.devops.gitprivacy.preferences.shared.OriginalCommitDateEncoder;
import de.empri.devops.gitprivacy.preferences.shared.OriginalCommitDateEncoderFactory;
import de.empri.devops.gitprivacy.preferences.shared.OriginalCommitDateEncoder.DecodedDates;

public class CommitHasEncryptedDatesPropertyTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (!(receiver instanceof IRepositoryCommit)) {
			return false;
		}
		
		IRepositoryCommit commit = (IRepositoryCommit) receiver;
		Optional<OriginalCommitDateEncoder> encoderOptional = OriginalCommitDateEncoderFactory.build(commit.getRepository());
		if (encoderOptional.isPresent()) {
			RevCommit revCommit = commit.getRevCommit();
			Optional<DecodedDates> decode = encoderOptional.get().decode(revCommit.getFullMessage());
			if (decode.isPresent()) {
				return true;
			}
		}
		return false;
	}

}
