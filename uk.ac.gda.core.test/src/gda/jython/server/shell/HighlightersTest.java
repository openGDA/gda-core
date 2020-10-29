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

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.jline.utils.AttributedString.fromAnsi;
import static org.junit.Assert.assertSame;
import static org.mockito.AdditionalAnswers.returnsElementsOf;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.core.PyUnicode;
import org.python.util.InteractiveInterpreter;

@RunWith(MockitoJUnitRunner.class)
public class HighlightersTest {

	@Mock private PyObject highlighter;
	@Mock private LineReader reader;
	private MockedConstruction<InteractiveInterpreter> mockInterpreter;

	/** Answer used whenever an InterativeInterpreter's eval method is called */
	private Answer<PyObject> evalResponse = inv -> highlighter;

	@Before
	public void setup() throws Exception {
		mockInterpreter = Mockito.mockConstruction(InteractiveInterpreter.class, (mock, ctx) -> {
			when(mock.eval(anyString())).thenAnswer(evalResponse);
		});
	}

	@After
	public void clearup() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Highlighters.resetCache();
		mockInterpreter.closeOnDemand();
	}

	@Test
	public void testNullHighlighter() throws Exception {
		Highlighter nullHighlight = Highlighters.getHighlighter(null);
		AttributedString string = nullHighlight.highlight(reader, "abcd");
		assertThat(string, is(equalTo(new AttributedString("abcd"))));
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
		when(highlighter.__call__(new PyUnicode("abcd"))).thenReturn(new PyUnicode("efgh"));
		Highlighter high = Highlighters.getHighlighter("theme");

		assertThat(high.highlight(reader, "abcd"), is(fromAnsi("efgh")));
	}

	@Test
	public void testHighlightersAreCached() throws Exception {
		Highlighter high1 = Highlighters.getHighlighter("theme_one");
		Highlighter high2 = Highlighters.getHighlighter("theme_one");

		assertSame(high1, high2);
		assertThat(mockInterpreter.constructed().size(), is(1));
	}

	@Test
	public void testRetriesIfPygmentsNotAvailable() throws Exception {
		evalResponse = inv -> {throw new PyException();};

		Highlighters.getHighlighter("theme_one");
		Highlighters.getHighlighter("theme_one");
		assertThat(mockInterpreter.constructed().size(), is(2));
	}

	@Test
	public void testNullIfInvalidTheme() throws Exception {
		evalResponse = inv -> {throw new Highlighters.InvalidThemeException();};
		Highlighter high1 = Highlighters.getHighlighter("theme_one");
		Highlighter high2 = Highlighters.getHighlighter("theme_one");

		assertThat(mockInterpreter.constructed().size(), is(1));
		assertSame(high1, high2);
	}

	@Test
	public void testDifferentThemesHaveDifferentHighlighters() throws Exception {
		PyObject h1 = mock(PyObject.class);
		PyObject h2 = mock(PyObject.class);
		when(h1.__call__(new PyUnicode("abcd"))).thenReturn(new PyUnicode("theme1"));
		when(h2.__call__(new PyUnicode("abcd"))).thenReturn(new PyUnicode("theme2"));
		evalResponse = returnsElementsOf(asList(h1, h2));

		Highlighter high1 = Highlighters.getHighlighter("theme_one");
		Highlighter high2 = Highlighters.getHighlighter("theme_two");

		assertThat(high1.highlight(reader, "abcd"), is(AttributedString.fromAnsi("theme1")));
		assertThat(high2.highlight(reader, "abcd"), is(AttributedString.fromAnsi("theme2")));
	}
}
