/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

import static gda.jython.server.shell.JythonSyntaxChecker.SyntaxState.COMPLETE;
import static gda.jython.server.shell.JythonSyntaxChecker.SyntaxState.EXEC;
import static gda.jython.server.shell.JythonSyntaxChecker.SyntaxState.INCOMPLETE;
import static gda.jython.server.shell.JythonSyntaxChecker.SyntaxState.INVALID;
import static org.python.core.CompileMode.exec;
import static org.python.core.CompileMode.single;

import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.python.core.CompileMode;
import org.python.core.CompilerFlags;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.server.shell.JythonSyntaxChecker.SyntaxState;

public class JythonSyntaxChecker implements Function<String, SyntaxState> {
	public enum SyntaxState {COMPLETE, INCOMPLETE, EXEC, INVALID}
	private static final Logger logger = LoggerFactory.getLogger(JythonSyntaxChecker.class);
	/** Name used for input filename by compiler when building exceptions */
	private static final String INPUT_FILENAME = "<input>";

	/** Default compiler flags */
	private final CompilerFlags flags = Py.getCompilerFlags();

	/** Translator to handle GDA syntax mangling */
	private final UnaryOperator<String> translator;

	public JythonSyntaxChecker(UnaryOperator<String> translator) {
		this.translator = translator;
	}

	@Override
	public SyntaxState apply(String source) {
		logger.trace("Checking {}", source);
		if (source.endsWith("\\")) {
			return INCOMPLETE;
		}
		PyObject code;
		var multiline = source.contains("\n");
		if (multiline) {
			if (!source.endsWith("\n")) {
				return INCOMPLETE;
			}
			code = compile(source, exec);
		} else {
			code = compile(source, single);
		}
		if (code == null) {
			return INVALID;
		} else if (Py.None.equals(code)) {
			return INCOMPLETE;
		} else {
			return multiline
					? EXEC
					: COMPLETE;
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
	 * @param mode the Python compile mode used to compile the source
	 * @return Code object, None or <code>null</code> depending on validity of source
	 */
	private PyObject compile(String source, CompileMode mode) {
		try {
			PyObject code =  Py.compile_command_flags(translator.apply(source), INPUT_FILENAME, mode, flags, mode == CompileMode.single);
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
