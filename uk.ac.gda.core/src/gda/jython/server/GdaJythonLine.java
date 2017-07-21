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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jline.reader.ParsedLine;
import org.jline.reader.Parser.ParseContext;

/**
 * The result of splitting a line of input into words/parts ready for providing completions
 */
class GdaJythonLine implements ParsedLine {
	private static final Pattern PARSE = Pattern.compile("[^a-zA-Z0-9_]+");

	private final String word;
	private final int wordCursor;
	private final int wordIndex;
	private final List<String> words;
	private final String line;
	private final int cursor;

	@SuppressWarnings("unused") // context is passed and ignored. It's a strange API
	GdaJythonLine(String line, int posn, ParseContext context) {
		this.line = line;
		String before = line.substring(0, posn);
		String after = line.substring(posn);
		List<String> wordsBefore = Arrays.asList(PARSE.split(before, -1));
		List<String> wordsAfter = Arrays.asList(PARSE.split(after))
				.stream()
				.filter(w -> !w.isEmpty())
				.collect(Collectors.toList());
		cursor = posn;
		word = wordsBefore.get(wordsBefore.size()-1);
		wordCursor = word.length();
		// If the first character of the line is a non word, beforeWords will
		// start with an empty string and should be removed
		if (wordsBefore.get(0).isEmpty()) {
			// This is ugly but Arrays.asList returns its own version of
			// ArrayList which doesn't support remove
			wordsBefore = new ArrayList<>(wordsBefore);
			wordsBefore.remove(0);
		}
		wordIndex = wordsBefore.size()-1; //zero index words
		words = new ArrayList<>();
		words.addAll(wordsBefore);
		words.addAll(wordsAfter);
	}

	@Override
	public String word() {
		return word;
	}

	@Override
	public int wordCursor() {
		return wordCursor;
	}

	@Override
	public int wordIndex() {
		return wordIndex;
	}

	@Override
	public List<String> words() {
		return words;
	}

	@Override
	public String line() {
		return line;
	}

	@Override
	public int cursor() {
		return cursor;
	}
}