/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import gda.jython.server.shell.JythonSyntaxChecker.SyntaxState;

class JythonSyntaxCheckTest {

	JythonSyntaxChecker check = new JythonSyntaxChecker(s -> s);

	static Collection<Object[]> samples() {
		// Array of {line, expectedState}
		return Arrays.asList(new Object[][] {
			{"print('abcd')", COMPLETE},
			{"print(abcd)", COMPLETE},

			// multiline
			{"a = b\nc = d\ne = f\n", EXEC},
			{"if 1:\n\tprint(foo)\nelse:\n\tprint(bar)\na = b\n", EXEC},

			// Incomplete single
			{"if 1:", INCOMPLETE},
			{"print('''", INCOMPLETE},
			{"if 1:\n\tprint(foo)\nelse:", INCOMPLETE},
			{"if 1:\n\tprint(foo)", INCOMPLETE},
			{"if 2:\n\t\n\t\n\t", INCOMPLETE},

			// Incomplete exec
			{"a=b\nif 1:", INCOMPLETE},
			{"a=b\nprint('''", INCOMPLETE},
			{"a=b\nc=d", INCOMPLETE},

			// Invalid syntax
			{"if 1:\nprint('abcd')\n", INVALID},
			{"if 1:\n\tprint('abcd')\nelse:\n4\n", INVALID},
			{"a = b\n\tc = d\n", INVALID},
		});
	}

	@ParameterizedTest(name = "{0}({1})") // line(cursor))
	@MethodSource("samples")
	void testResult(String source, SyntaxState required) {
		assertThat(check.apply(source), is(required));
	}
}
