/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package gda.jython.server.shell.highlighter;

import static java.lang.Character.digit;

import java.util.Set;
import java.util.regex.Pattern;

import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.translator.CommandTokenizer;

public class TokenStreamHighlighter implements Highlighter {
	private static final Logger logger = LoggerFactory.getLogger(TokenStreamHighlighter.class);
	private static final Set<String> KEYWORDS = Set.of(
			"def", "class", "return", "if", "elif", "else", "for", "while", "break",
			"continue", "pass", "yield", "try", "except", "finally", "raise", "with",
			"in", "is", "not", "and", "or", "global", "nonlocal", "import", "from",
			"as", "lambda", "del", "assert", "print");

	public enum SyntaxClass {
		KEYWORD, // - Python keywords eg for, if, def etc
		NUMBER, // - Number literals eg 1, 23.45, 0x4B
		STRING, // - String literals eg "helloWorld"
		COMMENT, // - Commented code - anything following a #
		OPERATOR, // - Signs and operators eg +, -, / etc
		PUNCTUATION, // - Other punctuation eg (, ), "
		DEFAULT, // - anything else
	}

	@FunctionalInterface
	public interface Theme {
		public static final AttributedStyle NO_STYLE = new AttributedStyle();
		public static final Theme NONE = t -> NO_STYLE;
		AttributedStyle styleFor(SyntaxClass syn);
	}

	private Theme theme;

	public static Highlighter forTheme(Theme theme) {
		return new TokenStreamHighlighter(theme);
	}

	public static Highlighter forTheme(String theme) {
		return forTheme(BasicTheme.fromValue(theme));
	}


	private TokenStreamHighlighter(Theme theme) {
		this.theme = theme;
	}

	@Override
	public AttributedString highlight(LineReader reader, String buffer) {
		var tokens = new CommandTokenizer(buffer);
		var asb = new AttributedStringBuilder(buffer.length());
		for (var token: tokens) {
			switch (token.type) {
			case BRACKET, COMMA -> asb.append(token.text, theme.styleFor(SyntaxClass.PUNCTUATION));
			case OP -> asb.append(token.text, theme.styleFor(SyntaxClass.OPERATOR));
			case COMMENT -> asb.append(token.text, theme.styleFor(SyntaxClass.COMMENT));
			case STRING -> asb.append(token.text, theme.styleFor(SyntaxClass.STRING));
			case WORD -> {
				if (KEYWORDS.contains(token.text)) {
					asb.append(token.text, theme.styleFor(SyntaxClass.KEYWORD));
				} else if (isNumber(token.text)) {
					asb.append(token.text, theme.styleFor(SyntaxClass.NUMBER));
				} else {
					asb.append(token.text, theme.styleFor(SyntaxClass.DEFAULT));
				}
			}
			case WS, NL -> asb.append(token.text);
			}
		}
		return asb.toAttributedString();
	}

	@Override
	public void setErrorPattern(Pattern errorPattern) {
		logger.info("setting error to {}", errorPattern);
	}

	@Override
	public void setErrorIndex(int errorIndex) {
		logger.info("setting error index to {}", errorIndex);
	}

	private static boolean isNumber(String value) {
		try {
			Double.valueOf(value);
			return true;
		} catch (NumberFormatException e) {
			if (value.length() > 2) {
				return switch (value.substring(0, 2)) {
				case "0b", "0B" -> value.chars().skip(2).allMatch(c -> digit(c, 2) != -1);
				case "0o", "0O" -> value.chars().skip(2).allMatch(c -> digit(c, 8) != -1);
				case "0x", "0X" -> value.chars().skip(2).allMatch(c -> digit(c, 16) != -1);
				default -> false;
				};
			}
			return false;
		}
	}
}
