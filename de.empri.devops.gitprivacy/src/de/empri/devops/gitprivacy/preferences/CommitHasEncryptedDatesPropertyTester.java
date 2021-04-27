package de.empri.devops.gitprivacy.preferences;

import org.eclipse.core.expressions.PropertyTester;

public class CommitHasEncryptedDatesPropertyTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		System.out.println("###### HELLO FROM PROPERTY TESTER");
		return false;
	}

}
