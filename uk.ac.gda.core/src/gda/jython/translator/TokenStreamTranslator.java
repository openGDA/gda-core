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
import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

public class TokenStreamTranslator extends AliasingBase implements Translator {
	/** Map of opening to closing brackets */
	private static final Map<Token, Token> BRACKET_PAIRS = Map.of(
			BRACKET.token("{"), BRACKET.token("}"),
			BRACKET.token("["), BRACKET.token("]"),
			BRACKET.token("("), BRACKET.token(")")
	);

	@Override
	public String translate(String command) {
		var buffer = new StringBuilder();
		var reader = new CommandReader(command);
		Deque<Token> statement;
		while (!(statement = reader.nextStatement()).isEmpty()) {
			buffer.append(translateStatement(statement));
		}
		return buffer.toString();
	}

	/** Translate a single statement of GDA specific tokens into valid Jython (if possible) */
	private String translateStatement(Deque<Token> statement) {
		Token first;
		switch (statement.size()) {
		case 0:
			return "";
		case 1:
			return singleToken(statement.getFirst());
		default:
			first = statement.getFirst();
			if (WS.matches(first)) {
				return statement.removeFirst().text + translateStatement(statement);
			}
			var last = statement.getLast();
			if (NL.matches(last) || COMMENT.matches(last) || WS.matches(last)) {
				var token = statement.removeLast();
				return translateStatement(statement) + token.text;
			}
			if (hasAlias(first.text)) {
				return expandAlias(statement);
			}
			return statement.stream().map(t -> t.text).collect(joining());
		}
	}

	/**
	 * Translate a single token, by either returning as is or adding parentheses
	 * if the token refers to an aliased command.
	 *
	 * This is made into a special case (rather than being handled with other
	 * aliased commands) as many places rely on the previous inconsistent
	 * behaviour where vararg aliased don't add [] when the arg list is empty.
	 *
	 * @param token the single token to translate into Jython
	 * @return The Jython expression represented by this token
	 */
	private String singleToken(Token token) {
		if (hasAlias(token.text)) {
			return token.text + "()";
		}
		return token.text;
	}

	/** Expand an alias, inserting brackets and commas as required */
	private String expandAlias(Deque<Token> statement) {
		var alias = statement.removeFirst();
		var next = statement.peekFirst(); // can't be null as we only get here
											// if there are multiple tokens
		if (WS.matches(next)) {
			statement.removeFirst();
			var args = aliases.contains(alias.text) ? argsUntil(statement, null) : "[" + argsUntil(statement, null) + "]";
			return alias.text + "(" + args + ")";
		} else {
			// Don't treat method calls or indexing on aliased names as a method call
			return alias.text + statement.stream().map(t -> t.text).collect(joining());
		}
	}

	/**
	 * Read the stream of tokens until the sentinel token is reached, with no
	 * processing or modification.
	 * <br>
	 * There is no modification of nested lists/parentheses (tokens are returned as
	 * they are read) but nested pairs of parentheses will be returned complete
	 * and will not return early, even if they match the sentinel token.
	 * <br>
	 * eg, parsing <pre>1, (2, 3), 4), 6</pre> with ')' as the sentinel will return
	 * <pre>1, (2, 3), 4)</pre> and won't return early on the first ')' token.
	 * @param tokens The stream of tokens to read
	 * @param sentinel The final token to read
	 */
	private static void appendUntil(StringBuilder buffer, Deque<Token> tokens, Token sentinel) {
		var token = tokens.pollFirst();
		while (token != null && !token.equals(sentinel)) {
			buffer.append(token.text);
			if (BRACKET.matches(token)) {
				var closing = BRACKET_PAIRS.get(token);
				if (closing != null) {
					appendUntil(buffer, tokens, closing);
				}
			}
			token = tokens.pollFirst();
		}
		if (token != null) {
			buffer.append(token.text);
		}
	}

	/**
	 * Parse the stream of tokens until the given sentinel token (or the end of
	 * the stream is reached) into a single expression. If the sentinel value is
	 * reached within a nested scope, it will not count as having been reached
	 * for the purpose of the outer call. This allows nested lists to be treated
	 * as individual elements of the outer list correctly.
	 * @param tokens The stream of tokens to read
	 * @param sentinel The final token to read.
	 * @return The parsed arguments joined by commas
	 */
	private static String argsUntil(Deque<Token> tokens, Token sentinel) {
		var buffer = new TokenBuffer();
		var token = tokens.pollFirst();
		while (token != null && !token.equals(sentinel)) {
			if (BRACKET.matches(token)){
				var closing = BRACKET_PAIRS.get(token);
				if (closing == null) {
					buffer.push(token);
				} else {
					String brackets;
					if ("[".equals(token.text)) {
						brackets = argsUntil(tokens, closing);
					} else {
						var nested = new StringBuilder();
						appendUntil(nested, tokens, closing);
						brackets = nested.toString();
					}
					buffer.push(WORD.token(token.text + brackets));
				}
			} else {
				buffer.push(token);
			}
			token = tokens.pollFirst();
		}
		var suffix = token == null ? "" : token.text;
		return buffer.toFullCommand() + suffix;
	}

	/**
	 * An argument parser that splits a stream of tokens into argument groupings.
	 *
	 * The tokens passed in are assumed to all be at the same level of nesting so
	 * any brackets (of any kind) are treated the same way as variable names/strings
	 * etc. WS between subsequent arguments is preserved with it being after the
	 * inserted comma if it is the only separator between terms
	 */
	private static class TokenBuffer {
		/** The previously parsed elements of the current expression */
		List<String> args = new ArrayList<>();
		/** The partial element that is currently being parsed */
		List<Token> buffer = new ArrayList<>();
		/** The last non WS token in the current (partial) element */
		Token lastNonWS;
		/**
		 * The last WS passed in - not added directly to buffer as it may form the
		 * start of the next element.
		 */
		Token trailingWS;

		public void push(Token t) {
			switch (t.type) {
				// if a bracket has made it this far there's going to be a syntax error anyway so treat it as a word
				case BRACKET -> push(WORD.token(t.text));
				case COMMA -> {
					lastNonWS = t;
					pushWS();
					pushArg();
				}
				case NL -> throw new IllegalArgumentException("New lines should be in single statements");
				case WS -> {
					if (buffer.isEmpty()) {
						buffer.add(t);
					} else {
						trailingWS = t;
					}
				}
				default -> {
					if (trailingWS != null && (WORD.matches(t) || STRING.matches(t) || OP.matches(t))) {
						pushArg();
					}
					pushWS();
					lastNonWS = t;
					buffer.add(t);
				}
			}
		}
		/** Push any trailing WS onto the end of the current element buffer */
		private void pushWS() {
			if (trailingWS != null) {
				buffer.add(trailingWS);
				trailingWS = null;
			}
		}
		/** Push the current partial element onto the list of parsed arguments if it is a valid argument */
		private void pushArg() {
			if (COMMA.matches(lastNonWS) || (!buffer.isEmpty() && !(OP.matchesOrNull(lastNonWS) || COMMENT.matchesOrNull(lastNonWS)))) {
				args.add(buffer.stream().map(t -> t.text).collect(joining()));
				buffer.clear();
				pushWS();
				lastNonWS = null;
			}
		}

		public String toFullCommand() {
			pushWS();
			pushArg();
			return args.stream().collect(joining(","));
		}
		@Override
		public String toString() {
			return "Buffer(" + args.stream().collect(joining(", ")) + ", " + buffer.toString() + ")";
		}
	}
}
