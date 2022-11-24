/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package gda.jython.translator;

import static gda.jython.translator.Type.BRACKET;
import static gda.jython.translator.Type.COMMA;
import static gda.jython.translator.Type.COMMENT;
import static gda.jython.translator.Type.NL;
import static gda.jython.translator.Type.OP;
import static gda.jython.translator.Type.STRING;
import static gda.jython.translator.Type.WORD;
import static gda.jython.translator.Type.WS;
import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CommandTokenizer {

	private static final int NONE = -1;

	private static final Set<Integer> WORD_CHARS;
	private static final Set<String> STRING_LITERAL_PREFIXES;
	private static final Map<Token, Token> BRACKET_MAP;
	static {
		var word = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_1234567890";
		WORD_CHARS = word.chars().boxed().collect(toSet());
		STRING_LITERAL_PREFIXES = Set.of("r", "u", "ur", "R", "U", "UR", "Ur", "uR", "b", "B", "br", "Br", "bR", "BR");
		BRACKET_MAP = Map.of(
				BRACKET.token(']'), BRACKET.token('['),
				BRACKET.token(')'), BRACKET.token('('),
				BRACKET.token('}'), BRACKET.token('{')
				);
	}

	private StringBuilder buffer;
	private StringReader command;

	private int next = NONE;

	private LinkedList<Token> bracketStack = new LinkedList<>();

	public CommandTokenizer(String command) {
		this.command = new StringReader(command);
		this.buffer = new StringBuilder();
	}

	public List<Token> tokens() {
		var tokens = new ArrayList<Token>();
		Token token;
		while ((token = nextToken()) != null) {
			tokens.add(token);
		}
		return tokens;
	}

	public Token nextToken() {
		int c;
		buffer.setLength(0);
		if (next != NONE) {
			c = next;
			next = NONE;
		} else {
			c = read();
		}
		return switch (c) {
		case '\'', '"' -> STRING.token(readString(c));
		case '\r' -> {
			next = read();
			if (next == '\n') {
				next = NONE;
				yield NL.token("\r\n");
			}
			yield NL.token(c);
		}
		case ';' -> {
			if (!bracketStack.isEmpty()) {
				// This is invalid syntax but leave it for the interpreter
				yield OP.token(c);
			} else {
				// ';' isn't really a new line but it often has the same meaning
				yield NL.token(c);
			}
		}
		case '\n' -> {
			if (bracketStack.isEmpty()) {
				yield NL.token(c);
			} else {
				yield WS.token(readWhitespace(c));
			}
		}
		case ' ', '\t' -> WS.token(readWhitespace(c));
		case NONE -> null;
		case '[', '(', '{' -> {
			var open = BRACKET.token(c);
			bracketStack.add(open);
			yield open;
		}
		case ']', ')', '}' -> {
			var close = BRACKET.token(c);
			if (!bracketStack.isEmpty() && (BRACKET_MAP.get(close).equals(bracketStack.getLast()))) {
				bracketStack.removeLast();
			}
			// else invalid syntax but let the interpreter deal with the problem
			yield close;
		}
		case '\\' -> {
			// line continuation
			next = read();
			if (next == '\n') {
				next = NONE;
				yield WS.token("\\\n");
			}
			// This is going to fail but let the interpreter deal with it
			yield OP.token('\\');
		}
		case '<', '>', '=', '*', '+', '-', '/', ':', '!', '%', '^', '&', '|', '@', '~' -> OP.token(c);
		case ',' -> COMMA.token(c);
		case '#' -> COMMENT.token(readComment(c));
		default -> {
			String word = readWord(c);
			if (STRING_LITERAL_PREFIXES.contains(word) && (next == '"' || next == '\'')) {
				String string = readString(next);
				// buffer isn't cleared so string will include prefix
				yield STRING.token(string);
			}
			yield WORD.token(word);
		}
		};
	}

	private String readComment(int c) {
		buffer.append((char)c);
		next = read();
		while (next > 0 && next != '\n') {
			buffer.append((char)next);
			next = read();
		}
		return buffer.toString();
	}

	private String readWord(int c) {
		buffer.append((char)c);
		next = read();
		while (WORD_CHARS.contains(next)) {
			buffer.append((char)next);
			next = read();
			if (next == '\\') {
				// line continuation character
				var x = read();
				if (x == '\n') {
					// ignore newline
					next = read();
				} else {
					// else syntax error but pass it to interpreter anyway
					buffer.append(next);
					buffer.append(x);
					next = read();
				}
			}
		}
		return buffer.toString();
	}

	private String readWhitespace(int c) {
		buffer.append((char)c);
		next = read();
		while (next == ' ' || next == '\t' || (next == '\n' && !bracketStack.isEmpty())) {
			buffer.append((char)next);
			next = read();
			if (next < 0) break;
			if (next == '\\') {
				// line continuation character
				var x = read();
				if (x < 0) break;
				if (x == '\n') {
					// ignore newline
					next = read();
				} else {
					// else syntax error but pass it to interpreter anyway
					buffer.append(next);
					buffer.append(x);
					next = read();
				}
			}
		}
		return buffer.toString();
	}

	private String readString(int c) {
		var triple = false;
		var req = 1;
		buffer.append((char)c);
		next = read();
		buffer.append((char)next);
		if (next == c) {
			next = read();
			if (next == c) {
				triple = true;
				req = 3;
				buffer.append((char)next);
			} else {
				return buffer.toString();
			}
		}

		while ((next = read()) > 0) {
			if (next == '\\') {
				buffer.append((char)next);
				var q = read();
				if (q < 0) break;
				buffer.append((char)q);
			} else if (next == c) {
				buffer.append((char)next);
				req -= 1;
				if (req == 0) {
					next = NONE;
					return buffer.toString();
				}
			} else if (next == '\n' && !triple) {
				// This is invalid python, but leave it as written so that errors get reported correctly
				return buffer.toString();
			} else {
				buffer.append((char)next);
				req = triple ? 3 : 1;
			}
		}
		return buffer.toString();
	}

	private int read() {
		try {
			return command.read();
		} catch (IOException e) {
			return -1;
		}
	}
}
