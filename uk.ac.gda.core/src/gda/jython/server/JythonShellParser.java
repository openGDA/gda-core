/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package gda.jython.server;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;

import org.jline.reader.EOFError;
import org.jline.reader.ParsedLine;
import org.jline.reader.Parser;
import org.jline.reader.SyntaxError;
import org.python.core.CompileMode;
import org.python.core.CompilerFlags;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Parser} implementation to check syntax of Python interactive source code.
 * <p>
 * If code is valid but incomplete, parsing should fail to enable multi-line input editing.
 * If code is complete but invalid, parsing should succeed to let the interpreter handle the error.
 *
 * @since 9.8
 */
public class JythonShellParser implements Parser {
	private static final Logger logger = LoggerFactory.getLogger(JythonShellParser.class);

	/** This is never used anywhere but is required by the compile command so it's here */
	private static final boolean STDPROMPT = false;
	/** The compile mode used when checking Python syntax. Single allows incomplete commands. */
	private static final CompileMode MODE = CompileMode.single;
	/** Default compiler flags */
	private static final CompilerFlags FLAGS = Py.getCompilerFlags();
	/** Name used for input filename by compiler when building exceptions */
	private static final String INPUT_FILENAME = "<input>";

	/** Translator to handle GDA syntax mangling */
	private final Function<String, String> translator;

	/**
	 * Create a {@link JythonShellParser parser} that does no translation before checking commands
	 * for completeness.
	 */
	public JythonShellParser() {
		this(s -> s);
	}

	/**
	 * Create a {@link JythonShellParser parser} that passes commands through a translator function before
	 * checking if the command is complete.
	 * <p>
	 * The commands are returned untranslated from the parse method.
	 * @param translator A function used to translate commands before checking for completeness.
	 */
	public JythonShellParser(Function<String, String> translator) {
		requireNonNull(translator, "Translator must not be null");
		this.translator = translator;
	}

	/**
	 * Convert a command from the shell into a {@link ParsedLine}. If the command is incomplete
	 * or is still being edited (cursor is part way through command), throw an {@link EOFError} to prompt the
	 * user for more. If the command is invalid, parse it as complete and let the interpreter handle the errors.
	 * @param command The command to parse
	 * @param cursor The position of the cursor within the command
	 * @param context The reason the line is being parsed
	 *
	 * @return A {@link GdaJythonLine}
	 */
	@Override
	public ParsedLine parse(String command, int cursor, ParseContext context) throws SyntaxError {
		logger.trace("cursor: {}/{}, context: {}", cursor, command.length(), context);
		checkParseErrors(command, cursor, context);
		return new GdaJythonLine(command, cursor);
	}

	/**
	 * Try to compile the Python code if necessary. Does not check syntax if the context is not
	 * {@link ParseContext#ACCEPT_LINE ACCEPT_LINE} or if the cursor is not at the end of the source.
	 * <p>
	 * If the cursor is midway through the source, it is assumed to be incomplete.
	 * @param source The Python source to be checked
	 * @param cursor The current cursor position
	 * @param context The reason for the source to be parsed
	 * @throws EOFError if source is valid but incomplete
	 */
	private void checkParseErrors(String source, int cursor, ParseContext context) {
		if (!context.equals(ParseContext.ACCEPT_LINE)) {
			// If we're not trying to accept the line to run it, it doesn't matter about syntax checking
			return;
		}
		if (cursor < source.length()) {
			// We're still editing the source
			throw new EOFError(0, 0, "still editing"); // Not used anywhere
		}
		PyObject code = compilePython(source);
		if (Py.None.equals(code)) {
			logger.trace("Code is incomplete", code);
			throw new EOFError(0, 0, "incomplete code"); // Not used anywhere
		}
	}

	/**
	 * Try and compile the input as python code.<br>
	 * <ul>
	 * <li>If the code is complete, return a Python code object.</li>
	 * <li>If the code is correct but incomplete, return {@link Py#None}</li>
	 * <li>If the code is incorrect (syntax error) return <code>null</code></li>
	 * </ul>
	 * @param source Python source from user input
	 * @return Code object, None or <code>null</code> depending on validity of source
	 */
	private PyObject compilePython(String source) {
		try {
			PyObject code =  Py.compile_command_flags(translator.apply(source), INPUT_FILENAME, MODE, FLAGS, STDPROMPT);
			logger.trace("Compiled code to: {}", code);
			return code;
		} catch (PyException e) {
			// Could throw SyntaxError here but for now it is ignored by Jline
			// see https://github.com/jline/jline3/issues/74
			logger.trace("Error compiling command", e);
			return null;
		}
	}
}
