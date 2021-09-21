package de.empri.devops.gitprivacy.preferences.initialization;

import static java.util.regex.Pattern.compile;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.empri.devops.gitprivacy.preferences.initialization.InitializeCommandHandler.State;

public enum HookParseStateMachine {

	INITIAL {
		@Override
		public State parse(State state) throws IOException {
			state.nextLine();
			state.parser = DEFAULT;
			return state;
		}
	},
	DEFAULT {
		@Override
		public State parse(State state) throws IOException {
			if (state.currentLine == null) {
				for (ErrorValue errorValue : state.errors) {
					// clean up additional newlines
					errorValue.fix = errorValue.fix.trim();
				}
				state.parser = FINISHED;
				return state;
			}
			Matcher matcher = HOOK_CONFLICT.matcher(state.currentLine);
			if (matcher.matches()) {
				ErrorValue error = new ErrorValue();
				error.hookName = matcher.group(HOOKNAME_GROUP_NAME);
				state.errors.add(error);
				state.parser = SEARCHING_FIX;
				state.nextLine();
				return state;
			}
			matcher = ALREADY_INSTALLED.matcher(state.currentLine);
			if (matcher.matches()) {
				state.alreadyInstalled.add(matcher.group(HOOKNAME_GROUP_NAME));
				state.nextLine();
				return state;
			}
			matcher = NEWLY_INSTALLED.matcher(state.currentLine);
			if (matcher.matches()) {
				state.newlyInstalled.add(matcher.group(HOOKNAME_GROUP_NAME));
				state.nextLine();
				return state;
			}
			state.nextLine();
			return state;
		}
	},
	SEARCHING_FIX {

	@Override
		public State parse(State state) throws IOException {
			if (HOOK_CONFLICT.matcher(state.currentLine).matches()
					|| ALREADY_INSTALLED.matcher(state.currentLine).matches()
					|| NEWLY_INSTALLED.matcher(state.currentLine).matches()) {
				state.parser = DEFAULT;
				return state;
			}

			if (state.currentLine.startsWith("#!/")) {
				state.currentError().fix = state.currentLine;
				state.nextLine();
				state.parser = PARSING_FIX;
				return state;
			}
			state.nextLine();
			return state;
		}

	},
	PARSING_FIX {
		@Override
		public State parse(State state) throws IOException {
			if (HOOK_CONFLICT.matcher(state.currentLine).matches()
					|| ALREADY_INSTALLED.matcher(state.currentLine).matches()
					|| NEWLY_INSTALLED.matcher(state.currentLine).matches()) {
				state.parser = DEFAULT;
				return state;
			}

			state.currentError().fix += "\n" + state.currentLine;
			state.nextLine();
			return state;
		}
	},
	FINISHED {
		@Override
		public State parse(State state) throws IOException {
			return state;
		}
	};

	private static final String HOOKNAME_GROUP_NAME = "hookname";
	private static final Pattern HOOK_CONFLICT = compile(
			"^A Git hook already exists at.*\\/(?<" + HOOKNAME_GROUP_NAME + ">\\w+-?\\w+)$");
	private static final Pattern ALREADY_INSTALLED = compile(
			"^(?<" + HOOKNAME_GROUP_NAME + ">\\w+-?\\w+) hook is already installed.*$");
	private static final Pattern NEWLY_INSTALLED = compile(
			"^Installed (?<" + HOOKNAME_GROUP_NAME + ">\\w+-?\\w+) hook.*$");

	public abstract State parse(State state) throws IOException;



		public class ErrorValue {
			public String fix;
			public String hookName;
		}

}
