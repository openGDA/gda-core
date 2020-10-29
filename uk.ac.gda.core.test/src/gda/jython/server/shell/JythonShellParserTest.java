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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.jline.reader.Parser.ParseContext.ACCEPT_LINE;
import static org.jline.reader.Parser.ParseContext.COMPLETE;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.Function;

import org.jline.reader.EOFError;
import org.jline.reader.ParsedLine;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.core.ThreadState;

public class JythonShellParserTest {

	@Rule
	public MockitoRule rule = MockitoJUnit.rule();

	@Mock private Function<String, String> translate;
	@Mock MockedStatic<Py> pyMock;

	private JythonShellParser parser;

	private MockedConstruction<GdaJythonLine> mockedLine;

	@Before
	public void setup() throws Exception {
		when(translate.apply(anyString())).thenAnswer(i -> i.getArgument(0));
		parser = new JythonShellParser(translate);

		mockedLine = Mockito.mockConstruction(GdaJythonLine.class, (mock, context) -> {
			when(mock.line()).thenReturn((String) context.arguments().get(0));
			when(mock.cursor()).thenReturn((Integer) context.arguments().get(1));
		});

	}

	@After
	public void closeMock() {
		mockedLine
		.close();
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
		pyMock.verify(() -> Py.compile_command_flags(eq("dcba"), anyString(), any(), any(), anyBoolean()));
	}

	@Test
	public void testDefaultTranslator() throws Exception {
		validPython();

		parser = new JythonShellParser();
		parser.parse("abcd", 4, ACCEPT_LINE);
		pyMock.verify(() -> Py.compile_command_flags(eq("abcd"), anyString(), any(), any(), anyBoolean()));

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

	private void invalidPython() {
		when(Py.compile_command_flags(anyString(), anyString(), any(), any(), anyBoolean()))
				.thenThrow(PyException.class);
	}

	private void incompletePython() {
		when(Py.compile_command_flags(anyString(), anyString(), any(), any(), anyBoolean()))
				.thenReturn(Py.None);
	}
}
