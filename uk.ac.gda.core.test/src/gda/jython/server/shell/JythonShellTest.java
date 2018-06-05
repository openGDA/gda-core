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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
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
import static org.powermock.api.mockito.PowerMockito.verifyZeroInteractions;

import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import org.jline.keymap.KeyMap;
import org.jline.reader.Binding;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.Terminal.Signal;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import gda.configuration.properties.LocalProperties;
import gda.jython.JythonServerFacade;
import gda.scan.IScanDataPoint;
import gda.scan.ScanDataPoint;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LocalProperties.class,
	JythonServerFacade.class,
	Highlighters.class,
	LineReaderBuilder.class,
	})
public class JythonShellTest {

	@Mock JythonServerFacade jsf;
	@Mock Terminal terminal;
	@Mock KeyMap<Binding> keymap;
	@Mock PrintWriter writer;
	@Mock LineReader reader;
	@Mock JythonShellParser parser;
	JythonShell shell;

	@Before
	public void setup() throws Exception {
		PowerMockito.mockStatic(LocalProperties.class);
		PowerMockito.when(LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME)).thenReturn("example");
		PowerMockito.when(LocalProperties.get(anyString(), anyString())).thenAnswer(i -> i.getArgumentAt(1, String.class));

		// For running commands
		PowerMockito.mockStatic(JythonServerFacade.class);
		PowerMockito.when(JythonServerFacade.getCurrentInstance()).thenReturn(jsf);

		PowerMockito.mockStatic(Highlighters.class);
		PowerMockito.mockStatic(LineReaderBuilder.class);

		PowerMockito.whenNew(JythonShellParser.class).withAnyArguments().thenReturn(parser);

		LineReaderBuilder builder = mock(LineReaderBuilder.class, new SelfReturningAnswer(c -> LineReaderBuilder.class == c));
		PowerMockito.when(LineReaderBuilder.builder()).thenReturn(builder);
		doReturn(reader).when(builder).build();
		Map<String, KeyMap<Binding>> maps = new HashMap<>();
		maps.put("main", keymap);
		when(reader.getKeyMaps()).thenReturn(maps);

		when(terminal.writer()).thenReturn(writer);

		shell = new JythonShell(terminal);
	}

	@After
	public void clearup() {
		shell.close();
	}

	@Test
	public void testCreation() throws Exception {
		PowerMockito.verifyStatic(times(1));
		Highlighters.getHighlighter(null);
		PowerMockito.verifyNoMoreInteractions(Highlighters.class);

		PowerMockito.verifyStatic(times(1));
		JythonServerFacade.getCurrentInstance();
		PowerMockito.verifyNoMoreInteractions(JythonServerFacade.class);

		PowerMockito.verifyNew(JythonShellParser.class);
	}

	@Test
	public void testUserDefinedTheme() throws Exception {
		Map<String, String> env = new HashMap<>();
		env.put("GDA_THEME", "user_theme");
		new JythonShell(terminal, env).close();

		PowerMockito.verifyStatic(times(1));
		Highlighters.getHighlighter("user_theme");
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
		shell.run();
		shell.close();

		verify(jsf).runsource(eq("print 'abcd'"), any(InputStream.class));
	}

	@Test
	public void testPrintOutputWhileNotReading() throws Exception {
		when(reader.readLine(anyString()))
				.thenReturn("print 'abcd'")
				.thenThrow(new EndOfFileException());
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
		shell.run();
		shell.close();
		InOrder output = inOrder(writer);
		output.verify(writer).write("helloWorld");
		output.verify(writer).write("\n");
		output.verify(writer).write("helloWorld\n");
		output.verify(writer).checkError();
		output.verifyNoMoreInteractions(); // no extra newline added
	}

	@Test(expected=NullPointerException.class)
	public void testNullCommandThrows() throws Exception {
		// See DAQ-1427 (Closing SSH without CTRL+D causes a infinite loop)
		when(reader.readLine(anyString()))
				.thenReturn(null)
				.thenThrow(new IllegalStateException()); // Should not be called again

		shell.run();
		verifyZeroInteractions(jsf); // null command should not be run
	}

	@Test
	public void testWriteErrorStopsShell() {
		final AtomicBoolean error = new AtomicBoolean(false); // needs to be final so can't use boolean
		when(reader.readLine(anyString()))
				.thenReturn("print 'helloWorld'")
				.thenThrow(new IllegalStateException()); // Shouldn't be called twice
		when(jsf.runsource(eq("print 'helloWorld'"), any(InputStream.class))).thenAnswer(i -> {
			error.set(true);
			shell.write("helloWorld");
			return false;
		});
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

		when(reader.readLine(anyString()))
				.thenReturn("scan abc 0 1 1 xyz")
				.thenThrow(new EndOfFileException());
		when(jsf.runsource(anyString(), any()))
				.thenAnswer(i -> {
					shell.update(jsf, sdp0);
					shell.update(jsf, sdp1);
					return false;
				});

		shell.run();
		shell.close();
		InOrder output = inOrder(writer);
		output.verify(writer).write("abc\txyz\n");
		output.verify(writer).write("  0\t1.3\n");
		output.verify(writer).write("  1\t2.3\n");
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
