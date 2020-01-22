/*-
 * Copyright © 2017 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.jython.server.shell;

import static java.util.Objects.requireNonNull;
import static org.jline.keymap.KeyMap.alt;
import static org.jline.keymap.KeyMap.ctrl;
import static org.jline.reader.LineReader.ACCEPT_LINE;
import static org.jline.reader.LineReader.BACKWARD_CHAR;
import static org.jline.reader.LineReader.BACKWARD_WORD;
import static org.jline.reader.LineReader.BEGINNING_OF_LINE;
import static org.jline.reader.LineReader.DELETE_CHAR;
import static org.jline.reader.LineReader.DOWN_HISTORY;
import static org.jline.reader.LineReader.DOWN_LINE;
import static org.jline.reader.LineReader.DOWN_LINE_OR_SEARCH;
import static org.jline.reader.LineReader.END_OF_LINE;
import static org.jline.reader.LineReader.ERRORS;
import static org.jline.reader.LineReader.FORWARD_CHAR;
import static org.jline.reader.LineReader.FORWARD_WORD;
import static org.jline.reader.LineReader.HISTORY_FILE;
import static org.jline.reader.LineReader.KILL_WHOLE_LINE;
import static org.jline.reader.LineReader.LINE_OFFSET;
import static org.jline.reader.LineReader.MENU_COMPLETE;
import static org.jline.reader.LineReader.SECONDARY_PROMPT_PATTERN;
import static org.jline.reader.LineReader.UP_HISTORY;
import static org.jline.reader.LineReader.UP_LINE;
import static org.jline.reader.LineReader.UP_LINE_OR_SEARCH;
import static org.jline.reader.LineReader.WORDCHARS;
import static org.jline.reader.LineReader.YANK;
import static org.jline.utils.AttributedString.stripAnsi;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.jline.keymap.KeyMap;
import org.jline.reader.Binding;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReader.Option;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;
import org.jline.reader.Reference;
import org.jline.reader.UserInterruptException;
import org.jline.reader.Widget;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;
import org.jline.terminal.Terminal.Signal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.jython.GDAJythonInterpreter;
import gda.jython.IScanDataPointObserver;
import gda.jython.JythonServerFacade;
import gda.jython.completion.AutoCompletion;
import gda.scan.IScanDataPoint;
import gda.util.Version;

/**
 * JythonShell provides the REPL for interacting with the GDA server.
 * It offers code completion and persistent history.
 * <p>
 * It has no knowledge of the connection type
 */
public class JythonShell implements Closeable, gda.jython.Terminal, IScanDataPointObserver {
	private static final Logger logger = LoggerFactory.getLogger(JythonShell.class);

	/** Primary prompt for input */
	private static final String PS1 = ">>> ";
	/** Secondary prompt for additional lines of input */
	private static final String PS2 = "... ";
	/** File to store command history */
	private static final String JYTHON_SERVER_HISTORY_FILE = "jython_server.history";
	/** The banner text to be printed when a user first opens the shell */
	private static final String WELCOME_BANNER;
	/** The file containing the banner text template */
	private static final String BANNER_FILE_NAME = "welcome_banner.txt";

	/** The environment variable holding the user's preferred theme */
	private static final String THEME_ENVIRONMENT_VARIABLE = "GDA_THEME";
	/** Property used to define the default syntax highlighting theme */
	private static final String JYTHON_SHELL_THEME_PROPERTY = "gda.remote.colours";
	/** Default theme if none is set by user */
	private static final String DEFAULT_THEME;
	/**
	 * Widget reference to accept full buffer.
	 * <p>
	 * With multi-line editing, a return while editing the middle of the buffer is interpreted as a new line.
	 * This enables the full input to be submitted from any point.
	 */
	private static final String ACCEPT_BUFFER = "accept-full-buffer";
	/** Widget reference to the builtin {@link LineReader#ACCEPT_LINE}, allowing accept line to share the behaviour of {@link #ACCEPT_BUFFER} */
	/* Prefixing a reference with '.' calls the builtin widget */
	private static final String SUBMIT_LINE = "." + ACCEPT_LINE;
	/** Widget reference to move current line down in buffer - {@link #moveLineDown()} */
	private static final String MOVE_LINE_DOWN = "move-line-down";
	/** Widget reference to move current line up in buffer - {@link #moveLineUp()} */
	private static final String MOVE_LINE_UP = "move-line-up";
	/** Template to use for title. The shell number should be added for each instance*/
	private static final String TITLE_TEMPLATE;
	/** Counter to give shells a unique id (per server session) */
	private static final AtomicInteger counter = new AtomicInteger(0);
	static {
		String banner;
		try (InputStream in = JythonShell.class.getResourceAsStream(BANNER_FILE_NAME)) {
			banner = new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining("\n"));
		} catch (IOException e) {
			logger.error("Could not close InputStream for {}", BANNER_FILE_NAME, e);
			banner = "Welcome to GDA %s\n";
		}
		WELCOME_BANNER = String.format(banner, Version.getRelease());
		TITLE_TEMPLATE = String.format("%s GDA - %s %s (#%%d)", // eg "I11 GDA - 9.6.0 - (dummy) (#%d)"
				LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME),
				Version.getRelease(),
				LocalProperties.isDummyModeEnabled() ? "(dummy)": ""
				);
		DEFAULT_THEME = LocalProperties.get(JYTHON_SHELL_THEME_PROPERTY, null);
	}

	/** The GDA Server to run commands */
	private final JythonServerFacade server;
	/** The terminal to provide direct access to the user terminal */
	private final Terminal terminal;
	/** The terminal reader used to interact with the user */
	private final LineReaderImpl read;
	/** InputStream that reads input through a JlineLineReader */
	private final InputStream rawInput;
	/** The unique ID of this shell */
	private final int shellNumber;
	private volatile boolean running;
	/** Flag to let welcome output be coloured */
	private final boolean colour;

	/** List of widget references that involve moving the cursor in the buffer */
	private static final Collection<String> BUFFER_CHANGE_WIDGETS = Arrays.asList(FORWARD_CHAR, BACKWARD_CHAR,
			END_OF_LINE, BEGINNING_OF_LINE, FORWARD_WORD, BACKWARD_WORD, UP_LINE_OR_SEARCH,
			DOWN_LINE_OR_SEARCH, MOVE_LINE_UP, MOVE_LINE_DOWN, UP_HISTORY, DOWN_HISTORY, DELETE_CHAR);

	public JythonShell(Terminal term) throws Exception {
		this(term, new HashMap<>());
	}

	public JythonShell(Terminal term, Map<String, String> env) throws Exception {
		logger.info("Running SSH shell as {}", env.getOrDefault("USER", "UNKNOWN"));
		terminal = term;
		server = JythonServerFacade.getCurrentInstance();
		final String gdaVar = LocalProperties.getVarDir();
		File historyFile = new File(gdaVar, JYTHON_SERVER_HISTORY_FILE);
		String theme = env.getOrDefault(THEME_ENVIRONMENT_VARIABLE, DEFAULT_THEME);
		colour = theme != null && !theme.isEmpty();
		read = (LineReaderImpl) LineReaderBuilder.builder()
				.terminal(term)
				.appName("GDA")
				.completer(new GdaJythonCompleter())
				.parser(new JythonShellParser(GDAJythonInterpreter::translateScriptToGDA))
				.highlighter(Highlighters.getHighlighter(theme))
				.variable(HISTORY_FILE, historyFile)
				.variable(SECONDARY_PROMPT_PATTERN, PS2)
				.variable(LINE_OFFSET, 1) // number lines from 1 not 0
				.variable(ERRORS, 40)
					// ^^^ hack to work around jline hardcoded completion handling
					// https://github.com/jline/jline3/issues/147
				.variable(WORDCHARS, "") // Split words on everything (except alphanum)
				.build();
		read.unsetOpt(Option.HISTORY_REDUCE_BLANKS); // keep tabs/indents in history
		read.setOpt(Option.MENU_COMPLETE); // Show completion options as menu
		read.setOpt(Option.DISABLE_EVENT_EXPANSION); // prevent escape characters being ignored
		read.unsetOpt(Option.BRACKETED_PASTE);
		read.unsetOpt(Option.HISTORY_IGNORE_SPACE); // don't ignore lines that start with space
		rawInput = new JlineInputStream(terminal);
		shellNumber = counter.getAndIncrement();
	}

	/**
	 * Run the actual REPL. Blocks while running and only returns when EOFException is reached
	 */
	public void run() {
		logger.info("Starting jython shell {}", shellNumber);
		running = true;
		init();
		while (running) {
			try {
				runCommand(read.readLine(PS1));
			} catch (UserInterruptException uie) {
				terminal.writer().println("KeyboardInterrupt");
				continue;
			} catch (EndOfFileException eol) {
				logger.info("Closing shell");
				break;
			}
		}
	}

	/**
	 * Run given command on the server.
	 * <p>
	 * Prints a warning to users if incomplete command was passed (most likely from a parsing/translation error).
	 * @param command Jython command to run
	 */
	private void runCommand(String command) {
		requireNonNull(command, "Null command received from jline");
		logger.debug("Running command: {}", command);
		boolean incomplete = server.runsource(command, rawInput);
		if (incomplete) {
			logger.warn("Incomplete command was treated as complete by parser. Command: {}", command);
			rawWrite("Previous command was not executed correctly, please contact GDA support\n");
		}
	}

	/**
	 * Run the setup code that makes this shell usable<br>
	 * <ul>
	 * <li>Add this as listener to {@link IScanDataPoint IScanDataPoints}</li>
	 * <li>Add this as an output {@link Terminal}</li>
	 * <li>Set up keybindings {@link #setupKeybindings}</li>
	 * <li>Prints the terminal banner</li>
	 * <li>Sets the title</li>
	 * <p>
	 * Separate from constructor to prevent passing incomplete instance as listener
	 */
	private void init() {
		server.addOutputTerminal(this);
		server.addIScanDataPointObserver(this);
		setupKeybindings();
		rawWrite(colour ? WELCOME_BANNER : stripAnsi(WELCOME_BANNER));
		setTitle(String.format(TITLE_TEMPLATE, shellNumber));
	}

	/**
	 * Initialise the non-default key bindings
	 */
	private void setupKeybindings() {
		KeyMap<Binding> mainKeyMap = read.getKeyMaps().get(LineReader.MAIN);
		// Ctrl-space autocompletes
		mainKeyMap.bind(new Reference(MENU_COMPLETE), KeyMap.ctrl(' '));

		// Ctrl-up/down should scroll through history. If each 'line' of history is multiple lines,
		// scrolling one at a time is not ideal.
		mainKeyMap.bind(new Reference(UP_HISTORY), "\033[1;5A"); // ctrl-up
		mainKeyMap.bind(new Reference(DOWN_HISTORY), "\033[1;5B"); // ctrl-down

		// Alt-up/down should move the current line up and down in the current command - similar to eclipse
		read.getWidgets().put(MOVE_LINE_UP, () -> moveLine(UP_LINE));
		read.getWidgets().put(MOVE_LINE_DOWN, () -> moveLine(DOWN_LINE));
		mainKeyMap.bind(new Reference(MOVE_LINE_UP), "\033[1;3A"); // alt-up
		mainKeyMap.bind(new Reference(MOVE_LINE_DOWN), "\033[1;3B"); // alt-down

		// Alt-Enter should accept the full buffer even if the cursor is part way through
		mainKeyMap.bind(new Reference(ACCEPT_BUFFER), alt(ctrl('M')));
		mainKeyMap.bind(new Reference(ACCEPT_BUFFER), alt('\n'));
		read.getWidgets().put(ACCEPT_BUFFER, this::acceptBuffer);
		read.getWidgets().put(ACCEPT_LINE, () -> {
			int pre = read.getBuffer().cursor();
			read.callWidget(SUBMIT_LINE);
			int post = read.getBuffer().cursor();
			// There doesn't seem to be a sensible way to check if a line was accepted
			// so only clear right prompt if no newline character was entered.
			if (pre == post) read.setRightPrompt("");
			return true;
		});
		// Automatically show execute prompt if needed when the buffer is changed.
		BUFFER_CHANGE_WIDGETS.forEach(this::addAcceptLineUpdate);
	}

	/** Wrap the referenced widget to update the accept-line prompt after execution */
	private void addAcceptLineUpdate(String reference) {
		Optional<Widget> current = Optional.ofNullable(read.getWidgets().get(reference));
		current.ifPresent(w -> read.getWidgets().put(reference, () -> {
			boolean result = w.apply();
			updateAcceptLinePrompt();
			return result;
		}));
	}

	/** Show a prompt on the right of the terminal to indicate when alt-enter is required to submit a multiline command */
	private void updateAcceptLinePrompt() {
		int cursor = read.getBuffer().cursor();
		int length = read.getBuffer().length();
		String msg = cursor == length ? "" : "Alt-Enter: Execute   ";
		read.setRightPrompt("\033[1;31m" + msg + "");
	}

	/**
	 * Accept the full buffer as a command.
	 * <p>
	 * Accepting a line while in the middle of the buffer is interpreted as a new line.
	 * This moves the cursor to the end of the buffer then calls accept line.
	 *
	 * @return true if successful, false otherwise
	 */
	private boolean acceptBuffer() {
		try {
			logger.trace("Accepting full buffer");
			read.setRightPrompt("");
			read.getBuffer().cursor(read.getBuffer().length());
			read.callWidget(SUBMIT_LINE);
			return true;
		} catch (IllegalStateException ise) {
			return false;
		}
	}

	/**
	 * Move the current line of a multiline buffer one line
	 * @param moveReference the move to make between deleting the line and rewriting it
	 * @return true if successful
	 */
	private boolean moveLine(String moveReference) {
		if (read.getBuffer().toString().contains("\n")) {
			// kill whole line doesn't work correctly if the cursor is on the last line
			// As a work around, add a new line at the end of buffer if we're on the last line
			int cursor = read.getBuffer().cursor();
			if (!read.getBuffer().substring(cursor).contains("\n")) {
				int length = read.getBuffer().length();
				read.getBuffer().cursor(length);
				read.getBuffer().write('\n');
				read.getBuffer().cursor(cursor);
			}
			read.callWidget(KILL_WHOLE_LINE);
			read.callWidget(moveReference);
			read.callWidget(YANK); // pastes the line that was killed above
			read.callWidget(UP_LINE);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Unsubscribe this shell from receiving {@link IScanDataPoint} and terminal output.
	 */
	@Override
	public void close() {
		logger.debug("Closing {}", this);
		// Don't close the terminal here as it is managed by the connection
		server.deleteOutputTerminal(this);
		server.deleteIScanDataPointObserver(this);
	}

	/**
	 * Handle updates from {@link JythonServerFacade}. Most likely {@link IScanDataPoint}s
	 */
	@Override
	public void update(Object source, Object arg) {
		if (arg instanceof IScanDataPoint) {
			IScanDataPoint sdp = (IScanDataPoint) arg;
			// If its the first point in a scan print the header
			if (sdp.getCurrentPointNumber() == 0) {
				write(sdp.getHeaderString() + "\n");
			}
			write(sdp.toFormattedString() + "\n");
		}
	}

	/**
	 * Write to the terminal
	 * <p>
	 * If this instance is in the process of reading a new line when the output is being printed,
	 * clear the prompt line first, then print the output, then restore the prompt.
	 * This prevents half completed commands being disrupted by output from other terminals.
	 */
	@Override
	public void write(String output) {
		try {
			read.callWidget(LineReader.CLEAR);
			if (!"\n".equals(output)) {
				rawWrite(output);
				if (!output.endsWith("\n")) {
					rawWrite("\n");
				}
			}
			read.callWidget(LineReader.REDISPLAY);
			return;
		} catch (IllegalStateException iae) {
			// can't call widgets if the terminal is not in a readline call
			// not reading -> we don't need to preserve the prompt line
		}
		rawWrite(output);
	}

	/**
	 * Write to and flush the terminal
	 *
	 * @param output the String to be written
	 */
	private void rawWrite(String output) {
		// If this shell has failed to write before, don't keep writing - interrupting the
		// shell causes more output to be written ("KeyboardInterrupt") and can
		// end in a loop
		if (running) {
			terminal.writer().write(output);
			if (terminal.writer().checkError()) {
				running = false;
				logger.error("#{} - Error writing to output", shellNumber);
				// If this output is coming from a different source, this shell will be
				// waiting for input and need to be interrupted. SIGINT causes a UserInterruptException
				// and breaks out of the loop
				terminal.raise(Signal.INT);
			}
		}
	}

	/**
	 * Set the title of the terminal window used to connect to this shell.
	 * <br>
	 * Handles the escaping and control characters needed.
	 *
	 * @param title The title to be used
	 */
	private void setTitle(String title) {
		rawWrite(String.format("\033]0;%s\007", title));
	}

	/**
	 * Completer to convert completions provided by JythonServer into Candidates accepted by Jline
	 */
	private class GdaJythonCompleter implements Completer {
		@Override
		public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
			AutoCompletion ac = server.getCompletionsFor(line.line(), line.cursor());
			candidates
				.addAll(ac
						.getStrings()
						.stream()
						.map(v -> new Candidate(v, v, null, null, null, null, false))
						.collect(Collectors.toList()));
		}
	}

	@Override
	public String toString() {
		return String.format("JythonShell#%d", shellNumber);
	}
}
