/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jline.reader.ParsedLine;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import gda.jython.server.shell.JythonSyntaxChecker.SyntaxState;

class GdaJythonLineTest {

	/**
	 * Helper for holding expected results
	 */
	private static record ParseResult(
			String word,
			String line,
			int wordCursor,
			int wordIndex,
			int cursor,
			List<String> words) {}

	static Collection<Object[]> data() {
		// Array of {line, cursor, expectedResult}
		return Arrays.asList(new Object[][] {
			// ParseResult(word, line, wordCursor, wordIndex, cursor, words)
			{"ab", 2, new ParseResult("ab", "ab", 2, 0, 2, asList("ab"))},
			{"[ab]", 3, new ParseResult("ab", "[ab]", 2, 0, 3, asList("ab"))},
			{"[ ( ab ) ]", 5, new ParseResult("a", "[ ( ab ) ]", 1, 0, 5, asList("a", "b"))},
			{"ab=cd", 5, new ParseResult("cd", "ab=cd", 2, 1, 5, asList("ab", "cd"))},
			{"ab = cd", 7, new ParseResult("cd", "ab = cd", 2, 1, 7, asList("ab", "cd"))},
			{"ab cd", 5, new ParseResult("cd", "ab cd", 2, 1, 5, asList("ab", "cd"))},
			{"ab=cd", 3, new ParseResult("", "ab=cd", 0, 1, 3, asList("ab", "", "cd"))},
			{"ab=cd", 4, new ParseResult("c", "ab=cd", 1, 1, 4, asList("ab", "c", "d"))},
			{"ab>=cd", 4, new ParseResult("", "ab>=cd", 0, 1, 4, asList("ab", "", "cd"))},
			{"ab_cd", 4, new ParseResult("ab_c", "ab_cd", 4, 0, 4, asList("ab_c", "d"))},
			{"[abc, def, ghi]", 9, new ParseResult("def", "[abc, def, ghi]", 3, 1, 9, asList("abc", "def", "ghi"))},
			{"if 3 > 4:\n\tprint 'foobar'", 14, new ParseResult("pri", "\tprint 'foobar'", 3, 3, 4, asList("if", "3", "4", "pri", "nt", "foobar"))},
		});
	}

	@ParameterizedTest(name = "{0}({1})") // line(cursor))
	@MethodSource("data")
	void testLineParsing(String inputLine, int cursor, ParseResult expected) {
		check(new GdaJythonLine(inputLine, cursor, SyntaxState.COMPLETE), expected);
	}

	private void check(ParsedLine line, ParseResult expected) {
		assertThat("Word not correct", expected.word, is(line.word()));
		assertThat("Line not correct", expected.line, is(line.line()));
		assertThat("Word Cursor not correct", expected.wordCursor, is(line.wordCursor()));
		assertThat("Word Index not correct", expected.wordIndex, is(line.wordIndex()));
		assertThat("Cursor not correct", expected.cursor, is(line.cursor()));
		assertThat("Words not correct", expected.words, is(line.words()));
	}
}
