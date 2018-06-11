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

import static org.jline.reader.Parser.ParseContext.ACCEPT_LINE;
import static org.jline.reader.Parser.ParseContext.COMPLETE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.Function;

import org.jline.reader.EOFError;
import org.jline.reader.ParsedLine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.core.ThreadState;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Py.class,
})
public class JythonShellParserTest {

	@Mock private Function<String, String> translate;

	private JythonShellParser parser;

	@Before
	public void setup() throws Exception {
		when(translate.apply(anyString())).thenAnswer(i -> i.getArgumentAt(0, String.class));
		parser = new JythonShellParser(translate);
		PowerMockito.whenNew(GdaJythonLine.class)
				.withAnyArguments()
				.thenAnswer(i -> {
					GdaJythonLine line = mock(GdaJythonLine.class);
					when(line.line()).thenReturn(i.getArgumentAt(0, String.class));
					when(line.cursor()).thenReturn(i.getArgumentAt(1, Integer.class));
					return line;
				});
		PowerMockito.mockStatic(Py.class);
	}

	@Test
	public void testValidCompleteCommand() {
		validPython();

		String command = "print 'helloWorld'";
		ParsedLine line = parser.parse(command, 18, ACCEPT_LINE);
		assertEquals(18, line.cursor());
		assertEquals(command, line.line());
	}

	@Test(expected=EOFError.class)
	public void testIncomplete() throws Exception {
		incompletePython();

		String command = "if abcd:";
		parser.parse(command, 8, ACCEPT_LINE);
	}

	@Test
	public void testTranslatedAlias() throws Exception {
		validPython();

		String command = "scan abcd 1 2 1";
		String translated = "scan(abcd, 1, 2, 1)";
		when(translate.apply(command)).thenReturn(translated);
		ParsedLine line = parser.parse(command, 15, ACCEPT_LINE);
		assertEquals(15, line.cursor());
		assertEquals(command, line.line());
	}

	@Test
	public void testIgnoreWhenNotAcceptingLine() throws Exception {
		incompletePython();

		String command = "invalid/incomplete command";
		ParsedLine line = parser.parse(command, 12, COMPLETE);
		assertEquals(12, line.cursor());
		assertEquals(command, line.line());
	}

	@Test(expected=EOFError.class)
	public void testIncompleteWhenInMiddleOfCommand() throws Exception {
		validPython();

		String command = "if abcd:\n\tprint 'abcd'";
		parser.parse(command, 8, ACCEPT_LINE);
	}

	@Test
	public void testMiddleOfSingleLineTreatedAsComplete() throws Exception {
		validPython();

		String command = "print 'helloWorld'";
		ParsedLine line = parser.parse(command, 8, ACCEPT_LINE);
		assertEquals(8, line.cursor());
		assertEquals(command, line.line());
	}

	@Test
	public void testInvalidPythonDoesNotThrow() throws Exception {
		invalidPython();

		String command = "print a b c";
		ParsedLine line = parser.parse(command, 8, ACCEPT_LINE);
		assertEquals(8, line.cursor());
		assertEquals(command, line.line());
	}

	@Test
	public void testTranslationUsed() throws Exception {
		when(translate.apply("abcd")).thenReturn("dcba");
		parser.parse("abcd", 4, ACCEPT_LINE);

		PowerMockito.verifyStatic();
		Py.compile_command_flags(eq("dcba"), anyString(), any(), any(), anyBoolean());
		PowerMockito.verifyNoMoreInteractions(Py.class);
	}

	@Test
	public void testDefaultTranslator() throws Exception {
		validPython();

		parser = new JythonShellParser();
		parser.parse("abcd", 4, ACCEPT_LINE);
		PowerMockito.verifyStatic();
		Py.compile_command_flags(eq("abcd"), anyString(), any(), any(), anyBoolean());
	}

	private void validPython() {
		PyObject mockPy = mock(PyObject.class);
		when(Py.getThreadState()).thenReturn(mock(ThreadState.class));
		// This doesn't make sense when mocking a non-None object but it's the easiest way of avoiding NPEs
		when(mockPy.getType()).thenReturn(PyObject.TYPE);
		when(mockPy.__eq__(any(PyObject.class))).thenReturn(Py.False);
		when(Py.compile_command_flags(anyString(), anyString(), any(), any(), anyBoolean()))
				.thenReturn(mockPy);
	}

	@SuppressWarnings("unchecked")
	private void invalidPython() {
		when(Py.compile_command_flags(anyString(), anyString(), any(), any(), anyBoolean()))
				.thenThrow(PyException.class);
	}

	private void incompletePython() {
		when(Py.compile_command_flags(anyString(), anyString(), any(), any(), anyBoolean()))
				.thenReturn(Py.None);
	}
}
