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

import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.core.PySystemState;
import org.python.core.PyUnicode;
import org.python.util.InteractiveInterpreter;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AttributedString.class,
	Highlighters.class,
})
public class HighlightersTest {

	@Mock private InteractiveInterpreter interpreter;
	@Mock private PyObject highlighter;
	@Mock private LineReader reader;

	@Before
	public void setup() throws Exception {
		PowerMockito.whenNew(InteractiveInterpreter.class)
				.withAnyArguments()
				.thenAnswer(i -> interpreter);

		PowerMockito.mockStatic(AttributedString.class);
		when(interpreter.eval(anyString())).thenReturn(highlighter);
	}

	@After
	public void clearup() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Highlighters.resetCache();
	}

	@Test
	public void testNullHighlighter() throws Exception {
		PowerMockito.whenNew(AttributedString.class).withArguments("abcd").thenAnswer(i -> AttributedString.fromAnsi("abdc"));
		Highlighter nullHighlight = Highlighters.getHighlighter(null);
		nullHighlight.highlight(reader, "abcd");

		PowerMockito.verifyNew(AttributedString.class).withArguments("abcd");
	}

	@Test
	public void testNullHighlighterIsSingleton() throws Exception {
		Highlighter null1 = Highlighters.getHighlighter(null);
		Highlighter null2 = Highlighters.getHighlighter("");
		Highlighter null3 = Highlighters.getHighlighter("none");
		assertSame(null1, null2);
		assertSame(null2, null3);
	}

	@Test
	public void testValidTheme() throws Exception {
		when(highlighter.__call__(new PyUnicode("abcd"))).thenReturn(new PyUnicode("abcd"));
		Highlighter high = Highlighters.getHighlighter("theme");

		high.highlight(reader, "abcd");

		PowerMockito.verifyStatic();
		AttributedString.fromAnsi("abcd");
	}

	@Test
	public void testHighlightersAreCached() throws Exception {
		Highlighter high1 = Highlighters.getHighlighter("theme_one");
		Highlighter high2 = Highlighters.getHighlighter("theme_one");

		assertSame(high1, high2);
		verify(interpreter, times(1)).eval(anyString());
	}

	@Test
	public void testRetriesIfPygmentsNotAvailable() throws Exception {
		doThrow(new PyException()).when(interpreter).exec(anyString());

		Highlighters.getHighlighter("theme_one");
		Highlighters.getHighlighter("theme_one");

		PowerMockito.verifyNew(InteractiveInterpreter.class, times(2));
	}

	@Test
	public void testNullIfInvalidTheme() throws Exception {
		doThrow(new Highlighters.InvalidThemeException())
				.when(interpreter).exec(anyString());

		Highlighter high1 = Highlighters.getHighlighter("theme_one");
		Highlighter high2 = Highlighters.getHighlighter("theme_one");

		PowerMockito.verifyNew(InteractiveInterpreter.class, times(1));
		assertSame(high1, high2);
	}

	@Test
	public void testDifferentThemesHaveDifferentHighlighters() throws Exception {
		InteractiveInterpreter iI1 = mock(InteractiveInterpreter.class);
		InteractiveInterpreter iI2 = mock(InteractiveInterpreter.class);
		PowerMockito.whenNew(InteractiveInterpreter.class)
				.withArguments(any(PyObject.class), any(PySystemState.class))
				.thenReturn(iI1)
				.thenReturn(iI2);
		PyObject h1 = mock(PyObject.class);
		PyObject h2 = mock(PyObject.class);
		when(h1.__call__(new PyUnicode("abcd"))).thenReturn(new PyUnicode("theme1"));
		when(h2.__call__(new PyUnicode("abcd"))).thenReturn(new PyUnicode("theme2"));
		when(iI1.eval(anyString())).thenReturn(h1);
		when(iI2.eval(anyString())).thenReturn(h2);

		Highlighter high1 = Highlighters.getHighlighter("theme_one");
		Highlighter high2 = Highlighters.getHighlighter("theme_two");

		high1.highlight(reader, "abcd");
		high2.highlight(reader, "abcd");

		PowerMockito.verifyStatic();
		AttributedString.fromAnsi("theme1");
		PowerMockito.verifyStatic();
		AttributedString.fromAnsi("theme2");
	}
}
