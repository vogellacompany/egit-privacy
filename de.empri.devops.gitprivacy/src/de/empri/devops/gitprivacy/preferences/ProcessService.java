package de.empri.devops.gitprivacy.preferences;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.util.FS;

public class ProcessService {

	private FS fs;

	public ProcessService() {
		fs = FS.detect();
	}

	public Process start(String command, File workingDirectory) throws IOException {
		return start(command, new ArrayList<>(), workingDirectory);
	}

	public Process start(String command, List<String> args, File workingDirectory) throws IOException {
		ProcessBuilder processBuilder = fs.runInShell(command, args.toArray(new String[args.size()]));
		processBuilder.directory(workingDirectory);
		processBuilder.redirectErrorStream(true);
		return processBuilder.start();
	}

}
