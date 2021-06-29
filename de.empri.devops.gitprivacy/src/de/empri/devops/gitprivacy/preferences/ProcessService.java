package de.empri.devops.gitprivacy.preferences;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProcessService {

	public Process start(String command, File workingDirectory) throws IOException {
		List<String> cmdArgs = new ArrayList<>();
		ProcessBuilder processBuilder = new ProcessBuilder();
		cmdArgs.add("sh");
		cmdArgs.add("-c");
		cmdArgs.add(command + " \"$@\"");
		cmdArgs.add(command);
		processBuilder.command(cmdArgs);
		processBuilder.directory(workingDirectory);
		return processBuilder.start();
	}

}
