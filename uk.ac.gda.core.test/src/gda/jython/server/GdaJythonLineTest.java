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

package gda.jython.server;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jline.reader.ParsedLine;
import org.jline.reader.Parser.ParseContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class GdaJythonLineTest {

	/**
	 * Helper class for holding expected results
	 */
	private static class ParseResult {
		String word, line;
		int wordCursor, wordIndex, cursor;
		List<String> words;
		public ParseResult(String word, String line, int wordCursor, int wordIndex, int cursor, List<String> words) {
			this.word = word;
			this.line = line;
			this.wordCursor = wordCursor;
			this.wordIndex = wordIndex;
			this.cursor = cursor;
			this.words = words;
		}
	}

	@Parameters(name="{0}({1})") // line(cursor)
	public static Collection<Object[]> data() {
		// Array of {line, cursor, expectedResult}
		return Arrays.asList(new Object[][] {
			// ParseResult(word, line, wordCursor, wordIndex, cursor, words)
			{"ab", 2, new ParseResult("ab", "ab", 2, 0, 2, Arrays.asList("ab"))},
			{"[ab]", 3, new ParseResult("ab", "[ab]", 2, 0, 3, Arrays.asList("ab"))},
			{"[ ( ab ) ]", 5, new ParseResult("a", "[ ( ab ) ]", 1, 0, 5, Arrays.asList("a", "b"))},
			{"ab=cd", 5, new ParseResult("cd", "ab=cd", 2, 1, 5, Arrays.asList("ab", "cd"))},
			{"ab = cd", 7, new ParseResult("cd", "ab = cd", 2, 1, 7, Arrays.asList("ab", "cd"))},
			{"ab cd", 5, new ParseResult("cd", "ab cd", 2, 1, 5, Arrays.asList("ab", "cd"))},
			{"ab=cd", 3, new ParseResult("", "ab=cd", 0, 1, 3, Arrays.asList("ab", "", "cd"))},
			{"ab=cd", 4, new ParseResult("c", "ab=cd", 1, 1, 4, Arrays.asList("ab", "c", "d"))},
			{"ab>=cd", 4, new ParseResult("", "ab>=cd", 0, 1, 4, Arrays.asList("ab", "", "cd"))},
			{"ab_cd", 4, new ParseResult("ab_c", "ab_cd", 4, 0, 4, Arrays.asList("ab_c", "d"))},
			{"[abc, def, ghi]", 9, new ParseResult("def", "[abc, def, ghi]", 3, 1, 9, Arrays.asList("abc", "def", "ghi"))},
		});
	}

	@Parameter(0)
	public String inputLine;
	@Parameter(1)
	public int cursor;
	@Parameter(2)
	public ParseResult expected;

	@Test
	public void testLineParsing() {
		check(new GdaJythonLine(inputLine, cursor, ParseContext.COMPLETE), expected);
	}


	private void check(ParsedLine line, ParseResult expected) {
		assertEquals("Word not correct", expected.word, line.word());
		assertEquals("Line not correct", expected.line, line.line());
		assertEquals("Word Cursor not correct", expected.wordCursor, line.wordCursor());
		assertEquals("Word Index not correct", expected.wordIndex, line.wordIndex());
		assertEquals("Cursor not correct", expected.cursor, line.cursor());
		assertEquals("Words not correct", expected.words, line.words());
	}
}
