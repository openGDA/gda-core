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

package gda.jython.server.shell;

import static java.util.Objects.requireNonNull;

import java.util.function.UnaryOperator;

import org.jline.reader.EOFError;
import org.jline.reader.ParsedLine;
import org.jline.reader.Parser;
import org.jline.reader.SyntaxError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.server.shell.JythonSyntaxChecker.SyntaxState;

/**
 * A {@link Parser} implementation to check syntax of Python interactive source code.
 * <p>
 * If code is valid but incomplete, parsing should fail to enable multi-line input editing.
 * If code is complete but invalid, parsing should succeed to let the interpreter handle the error.
 *
 * @since 9.8
 */
class JythonShellParser implements Parser {
	private static final Logger logger = LoggerFactory.getLogger(JythonShellParser.class);

	private final JythonSyntaxChecker syntaxCheck = new JythonSyntaxChecker();
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
	public JythonShellParser(UnaryOperator<String> translator) {
		requireNonNull(translator, "Translator must not be null");
		syntaxCheck.setTranslator(translator);
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
		if (context.equals(ParseContext.ACCEPT_LINE)) {
			if (cursor < command.length() && command.contains("\n")) {
				// We're still editing the source
				throw new EOFError(0, 0, "still editing"); // Not used anywhere
			}
			if (syntaxCheck.apply(command) == SyntaxState.INCOMPLETE) {
				logger.trace("Code is incomplete");
				throw new EOFError(0, 0, "incomplete code"); // Not used anywhere
			}
		}
		return new GdaJythonLine(command, cursor);
	}
}
