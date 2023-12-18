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

import static gda.jython.server.shell.JythonSyntaxChecker.SyntaxState.COMPLETE;
import static gda.jython.server.shell.JythonSyntaxChecker.SyntaxState.EXEC;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import org.eclipse.scanning.api.script.ScriptExecutionException;
import org.jline.keymap.KeyMap;
import org.jline.reader.Binding;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;
import org.jline.terminal.Terminal.Signal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;

import gda.configuration.properties.LocalProperties;
import gda.jython.JythonServerFacade;
import gda.jython.server.shell.highlighter.TokenStreamHighlighter;
import gda.scan.IScanDataPoint;
import gda.scan.ScanDataPoint;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class JythonShellTest {

	@Mock JythonServerFacade jsf;
	@Mock Terminal terminal;
	@Mock KeyMap<Binding> keymap;
	@Mock PrintWriter writer;
	@Mock LineReaderImpl reader;
	@Mock JythonShellParser parser;
	JythonShell shell;
	@Mock MockedConstruction<JythonShellParser> jythonShellParserMock;
	@Mock MockedStatic<LocalProperties> localPropertiesMock;
	@Mock MockedStatic<JythonServerFacade> jythonServerFacadeMock;
	@Mock MockedStatic<TokenStreamHighlighter> highlightersMock;
	@Mock MockedStatic<LineReaderBuilder> lineReaderBuilderMock;

	@BeforeEach
	public void setup() throws Exception {
		Mockito.when(LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME)).thenReturn("example");
		Mockito.when(LocalProperties.get(anyString(), anyString())).thenAnswer(i -> i.getArgument(1, String.class));

		// For running commands
		Mockito.when(JythonServerFacade.getCurrentInstance()).thenReturn(jsf);

		LineReaderBuilder builder = mock(LineReaderBuilder.class, new SelfReturningAnswer(c -> LineReaderBuilder.class == c));
		Mockito.when(LineReaderBuilder.builder()).thenReturn(builder);
		doReturn(reader).when(builder).build();
		Map<String, KeyMap<Binding>> maps = new HashMap<>();
		maps.put("main", keymap);
		when(reader.getKeyMaps()).thenReturn(maps);

		when(terminal.writer()).thenReturn(writer);

		shell = new JythonShell(terminal);
	}

	@AfterEach
	public void clearup() {
		shell.close();
	}

	@Test
	public void testCreation() throws Exception {
		Mockito.verify(TokenStreamHighlighter.class, times(1));
		TokenStreamHighlighter.forTheme((String)null);
		Mockito.verifyNoMoreInteractions(TokenStreamHighlighter.class);

		Mockito.verify(JythonServerFacade.class, times(1));
		JythonServerFacade.getCurrentInstance();
		Mockito.verifyNoMoreInteractions(JythonServerFacade.class);

		assertEquals(1, jythonShellParserMock.constructed().size());
	}

	@Test
	public void testUserDefinedTheme() throws Exception {
		Map<String, String> env = new HashMap<>();
		env.put("GDA_THEME", "user_theme");
		new JythonShell(terminal, env).close();

		Mockito.verify(TokenStreamHighlighter.class, times(1));
		TokenStreamHighlighter.forTheme("user_theme");
	}

	@Test
	public void testInit() {
		when(reader.readLine(anyString())).thenThrow(new EndOfFileException());
		shell.run();

		verify(jsf).addOutputTerminal(shell);
		verify(jsf).addIScanDataPointObserver(shell);
		verifyNoMoreInteractions(jsf);
		verify(terminal.writer(), times(2)).write(anyString()); // Write banner and title
	}

	@Test
	public void testClose() throws Exception {
		when(reader.readLine(anyString())).thenThrow(new EndOfFileException());
		shell.run();
		shell.close();

		verify(jsf).deleteOutputTerminal(shell);
		verify(jsf).deleteIScanDataPointObserver(shell);
	}

	@Test
	public void testInterrupt() throws Exception {
		when(reader.readLine(anyString()))
				.thenThrow(new UserInterruptException("stuff"))
				.thenThrow(new EndOfFileException());
		shell.run();
		shell.close();

		verify(writer).println("KeyboardInterrupt");
		verify(reader, times(2)).readLine(anyString());
		verify(jsf, never()).runsource(anyString(), any(InputStream.class));
	}

	@Test
	public void testRunCommand() throws Exception {
		when(reader.readLine(anyString()))
				.thenReturn("print 'abcd'")
				.thenThrow(new EndOfFileException());
		when(reader.getParsedLine())
				.thenReturn(new GdaJythonLine("print 'abcd'", 12, COMPLETE));
		shell.run();
		shell.close();

		verify(jsf).runsource(eq("print 'abcd'"), any(InputStream.class));
	}

	@Test
	public void testPrintOutputWhileNotReading() throws Exception {
		when(reader.readLine(anyString()))
				.thenReturn("print 'abcd'")
				.thenThrow(new EndOfFileException());
		when(reader.getParsedLine())
				.thenReturn(new GdaJythonLine("print 'abcd'", 12, COMPLETE));
		doThrow(new IllegalStateException())
				.when(reader).callWidget(anyString());
		when(jsf.runsource(eq("print 'abcd'"), any())).thenAnswer(i -> {
			shell.write("helloWorld"); // output while not reading from jline
			return false;
		});
		shell.run();
		shell.close();
		InOrder output = inOrder(writer);
		output.verify(writer).write("helloWorld");
		output.verify(writer).checkError();
		output.verifyNoMoreInteractions(); // no extra newline
	}

	@Test
	public void testPrintOutputWhileReading() throws Exception {
		when(reader.readLine(anyString()))
				.thenAnswer(i -> {
					shell.write("helloWorld"); // output while reading from jline
					return "print 'abcd'";
				}).thenAnswer(i -> {
					shell.write("helloWorld\n");
					throw new EndOfFileException();
				});
		when(reader.getParsedLine())
				.thenReturn(new GdaJythonLine("print 'abcd'", 12, COMPLETE))
				.thenReturn(new GdaJythonLine("helloWorld\n", 11, COMPLETE));
		shell.run();
		shell.close();
		InOrder output = inOrder(writer);
		output.verify(writer).write("helloWorld");
		output.verify(writer).write("\n");
		output.verify(writer).write("helloWorld\n");
		output.verify(writer).checkError();
		output.verifyNoMoreInteractions(); // no extra newline added
	}

	@Test
	public void testNullCommandThrows() throws Exception {
		// See DAQ-1427 (Closing SSH without CTRL+D causes a infinite loop)
		when(reader.readLine(anyString()))
				.thenReturn(null)
				.thenThrow(new IllegalStateException()); // Should not be called again

		assertThrows(NullPointerException.class, shell::run);
	}

	@Test
	public void testWriteErrorStopsShell() {
		final AtomicBoolean error = new AtomicBoolean(false); // needs to be final so can't use boolean
		var command = "print 'helloWorld'";
		when(reader.readLine(anyString()))
				.thenReturn(command)
				.thenThrow(new IllegalStateException()); // Shouldn't be called twice
		when(jsf.runsource(eq(command), any(InputStream.class))).thenAnswer(i -> {
			error.set(true);
			shell.write("helloWorld");
			return false;
		});
		when(reader.getParsedLine())
				.thenReturn(new GdaJythonLine(command, command.length(), COMPLETE));
		when(writer.checkError()).thenAnswer(i -> error.get());

		shell.run();
		shell.close();

		verify(reader, times(1)).readLine(anyString());
		verify(jsf, times(1)).runsource(anyString(), any());
		verify(terminal).raise(Signal.INT);
	}

	@Test
	public void testScanDataPoint() throws Exception {
		IScanDataPoint sdp0 = new ScanDataPoint();
		sdp0.setCurrentPointNumber(0);
		sdp0.setScannableHeader(new String[] {"abc", "xyz"});
		sdp0.setScannablePositions(new Vector<>(Arrays.asList(0, 1.3)));
		sdp0.setScannableFormats(new String[][] {{"%.1f"}, {"%.1f"}});

		IScanDataPoint sdp1 = new ScanDataPoint();
		sdp1.setCurrentPointNumber(1);
		sdp1.setScannableHeader(new String[] {"abc", "xyz"});
		sdp1.setScannablePositions(new Vector<>(Arrays.asList(1, 2.3)));
		sdp1.setScannableFormats(new String[][] {{"%.1f"}, {"%.1f"}});

		var command = "scan abc 0 1 1 xyz";
		when(reader.readLine(anyString()))
				.thenReturn(command)
				.thenThrow(new EndOfFileException());
		when(jsf.runsource(anyString(), any()))
				.thenAnswer(i -> {
					shell.update(jsf, sdp0);
					shell.update(jsf, sdp1);
					return false;
				});
		when(reader.getParsedLine())
				.thenReturn(new GdaJythonLine(command, command.length(), COMPLETE));

		shell.run();
		shell.close();
		InOrder output = inOrder(writer);
		output.verify(writer).write("abc\txyz\n");
		output.verify(writer).write("  0\t1.3\n");
		output.verify(writer).write("  1\t2.3\n");
	}

	@Test
	void testMultilineCommand() throws ScriptExecutionException {
		var command = """
				def foo(a, b):
				    return a + b
				a = foo(1, 2)
				""";
		when(reader.readLine(anyString()))
				.thenReturn(command)
				.thenThrow(new EndOfFileException());
		when(reader.getParsedLine())
				.thenReturn(new GdaJythonLine(command, command.length(), EXEC));
		shell.run();
		shell.close();

		verify(jsf).executeCommand(eq(command), any(InputStream.class));
	}

	/**
	 * Answer for mocking builders that should return themselves
	 * <p>
	 * A explicit check is needed to be passed in as dynamic type checking at
	 * run time doesn't seem to work well with mocked objects.
	 * <p>
	 * Based on StackOverflow https://stackoverflow.com/a/8530200/1916917
	 */
	public static class SelfReturningAnswer implements Answer<Object>{
		private Predicate<Class<?>> check;
		/**
		 * @param typeCheck A predicate to decide if 'this' should be returned
		 * based on return type
		 */
		public SelfReturningAnswer(Predicate<Class<?>> typeCheck) {
			check = typeCheck;
		}
		/**
		 * If the return type of the called method matches this instance's
		 * type check, return the same mock object, otherwise return the default
		 */
		@Override
		public Object answer(InvocationOnMock invocation) throws Throwable {
			// For some reason
			// invocation.getMethod().getReturnType().isInstance(invocation.getMock())
			// always returns false so an explicit predicate is required.
			if (check.test(invocation.getMethod().getReturnType())) {
				return invocation.getMock();
			} else {
				return RETURNS_DEFAULTS.answer(invocation);
			}
		}
	}
}
