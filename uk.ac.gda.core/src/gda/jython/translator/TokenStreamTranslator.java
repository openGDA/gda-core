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

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TokenStreamTranslator extends AliasingBase implements Translator {
	private static final Logger logger = LoggerFactory.getLogger(TokenStreamTranslator.class);

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

	private String translateStatement(Deque<Token> statement) {
		Token first;
		switch (statement.size()) {
		case 0:
			return "";
		case 1:
			first = statement.getFirst();
			if (hasAlias(first.text)) {
				// This should be replaced with + "([])" for vararg but GDA now depends on the broken behaviour of GeneralTranslator
				return first.text + "()";
			}
			return first.text;
		default:
			// Check for indented text
			if (statement.getFirst().type == Type.WS) {
				var ws = statement.removeFirst();
				return ws.text + translateStatement(statement);
			}
			// Check for comments at end of statement
			if (statement.getLast().type == Type.COMMENT) {
				var last = statement.removeLast();
				return translateStatement(statement) + last.text;
			}
			// Remove newline while we translate the rest of the line
			if (statement.getLast().type == Type.NL) {
				var last = statement.removeLast();
				return translateStatement(statement) + last.text;
			}
			first = statement.removeFirst();
			if (hasAlias(first.text)) {
				var args = splitArguments(statement).collect(toList());
				if (args.size() == 1 && args.get(0).startsWith("(") && args.get(0).endsWith(")") ) {
					return first.text + args.get(0);
				} else {
					if (aliases.contains(first.text)) {
						return first.text + args.stream().collect(joining(", ", "(", ")"));
					} else {
						return first.text + args.stream().collect(joining(", ", "([", "])"));
					}
				}
			}
			return first.text + statement.stream().map(t -> t.text).collect(joining());
		}
	}

	private Stream<String> splitArguments(Deque<Token> statement) {
		List<List<Token>> args = new LinkedList<>();
		List<Token> current = new LinkedList<>();
		/*
		 * Keep a track of whether the previous token seen was whitespace. This is a bit of a back
		 * to allow commas to be optional while preventing whitespace before a comma causing a
		 * second command to be added, eg for [WORD, WS, COMMA, WS, WORD]
		 * This allows either the start of a new argument or a comma to mark the end of an argument,
		 * instead of treating WS as the delimiter.
		 */
		var trailing = false;
		var square = 0;
		var round = 0;
		var brace = 0;
		for (var t: statement) {
			switch (t.type) {
			case BRACKET:
				switch (t.text) {
				case "[":
					square += 1;
					break;
				case "(":
					round += 1;
					break;
				case "{":
					brace += 1;
					break;
				case "]":
					square -= 1;
					break;
				case ")":
					round -= 1;
					break;
				case "}":
					brace -= 1;
					break;
				default:
					logger.warn("unexpected bracket: {}", t);
				}
				if (trailing && !current.isEmpty()) {
					args.add(current);
					current = new LinkedList<>();
				}
				current.add(t);
				trailing = false;
				break;
			case COMMA:
				if (square == 0 && round == 0 && brace == 0) {
					args.add(current);
					current = new LinkedList<>();
				} else {
					current.add(t);
				}
				trailing = false;
				break;
			case WS:
				if (square == 0 && round == 0 && brace == 0) {
					trailing = true;
				} else {
					current.add(t);
				}
				break;
			default:
				if (trailing && !current.isEmpty()) {
					args.add(current);
					current = new LinkedList<>();
				}
				current.add(t);
				trailing = false;
			}
		}
		if (!current.isEmpty()) {
			args.add(current);
		}
		return args.stream()
				.map(a -> a.stream().map(t -> t.text).collect(joining()));
	}
}
