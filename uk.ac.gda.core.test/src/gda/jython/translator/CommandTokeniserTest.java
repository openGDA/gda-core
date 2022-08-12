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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import java.util.List;

import org.junit.jupiter.api.Test;

public class CommandTokeniserTest {
	@Test
	public void singleQuote() {
		assertThat(ct("'string'"), contains(STRING.token("'string'")));
		assertThat(ct("\"string\""), contains(STRING.token("\"string\"")));
	}
	@Test
	public void tripleQuote() {
		assertThat(ct("'''st'r'ing'''"), contains(STRING.token("'''st'r'ing'''")));
		assertThat(ct("'''st''ring'''"), contains(STRING.token("'''st''ring'''")));
		assertThat(ct("\"\"\"st\"r\"ing\"\"\""), contains(STRING.token("\"\"\"st\"r\"ing\"\"\"")));
	}
	@Test
	public void multilineQuote() {
		assertThat(ct("'''foo\nbar'''"), contains(STRING.token("'''foo\nbar'''")));
	}
	@Test
	public void stringLiteralPrefix() {
		assertThat(ct("r'raw string'"), contains(STRING.token("r'raw string'")));
	}
	@Test
	public void twoStrings() {
		assertThat(ct("'one''two'"), contains(STRING.token("'one'"), STRING.token("'two'")));
		assertThat(ct("''\"\""), contains(
				STRING.token("''"),
				STRING.token("\"\"")));
	}

	@Test
	public void escapedString() {
		assertThat(ct("'o\\nne\\''"), contains(STRING.token("'o\\nne\\''")));
	}

	@Test
	public void wsString() {
		assertThat(ct(" \t 'string'   "), contains(WS.token(" \t "), STRING.token("'string'"), WS.token("   ")));
	}

	@Test
	public void multilineCommand() {
		assertThat(ct("for i in range(10):\n\tprint(i)\n"), contains(
				WORD.token("for"),
				WS.token(" "),
				WORD.token("i"),
				WS.token(" "),
				WORD.token("in"),
				WS.token(" "),
				WORD.token("range"),
				BRACKET.token("("),
				WORD.token("10"),
				BRACKET.token(")"),
				OP.token(":"),
				NL.token("\n"),
				WS.token("\t"),
				WORD.token("print"),
				BRACKET.token("("),
				WORD.token("i"),
				BRACKET.token(")"),
				NL.token("\n")));
	}

	@Test
	public void multilineBrackets() {
		assertThat(ct("pos foo (1, \n2, 3)"), contains(
				WORD.token("pos"),
				WS.token(" "),
				WORD.token("foo"),
				WS.token(" "),
				BRACKET.token("("),
				WORD.token("1"),
				COMMA.token(","),
				WS.token(" \n"),
				WORD.token("2"),
				COMMA.token(","),
				WS.token(" "),
				WORD.token("3"),
				BRACKET.token(")")
				));
		assertThat(ct("a = [1,\n3]"), contains(
				WORD.token("a"),
				WS.token(" "),
				OP.token("="),
				WS.token(" "),
				BRACKET.token("["),
				WORD.token("1"),
				COMMA.token(","),
				WS.token("\n"),
				WORD.token("3"),
				BRACKET.token("]")));
	}

	@Test
	public void commandWithComment() {
		var command = "a = 42 # rest of line";
		assertThat(ct(command), contains(
				WORD.token("a"),
				WS.token(" "),
				OP.token("="),
				WS.token(" "),
				WORD.token("42"),
				WS.token(" "),
				COMMENT.token("# rest of line")));
	}

	@Test
	public void multilineWithComment() {
		assertThat(ct("for i in range(10): # comment on range \n\tprint(i)\n"), contains(
				WORD.token("for"),
				WS.token(" "),
				WORD.token("i"),
				WS.token(" "),
				WORD.token("in"),
				WS.token(" "),
				WORD.token("range"),
				BRACKET.token("("),
				WORD.token("10"),
				BRACKET.token(")"),
				OP.token(":"),
				WS.token(" "),
				COMMENT.token("# comment on range "),
				NL.token("\n"),
				WS.token("\t"),
				WORD.token("print"),
				BRACKET.token("("),
				WORD.token("i"),
				BRACKET.token(")"),
				NL.token("\n")));
	}

	@Test
	public void hexValue() {
		assertThat(ct("34 * 0x23"), contains(
				WORD.token("34"),
				WS.token(" "),
				OP.token("*"),
				WS.token(" "),
				WORD.token("0x23")));
	}

	@Test
	public void printTriple() {
		var command = "print('''foo\nbar''')";
		assertThat(ct(command), contains(
				WORD.token("print"),
				BRACKET.token("("),
				STRING.token("'''foo\nbar'''"),
				BRACKET.token(")")));
	}

	@Test
	public void lineContinuation() {
		assertThat(ct("a = 1 + \\\n 3"), contains(
				WORD.token("a"),
				WS.token(" "),
				OP.token("="),
				WS.token(" "),
				WORD.token("1"),
				WS.token(" "),
				OP.token("+"),
				WS.token(" "),
				WS.token("\\\n"),
				WS.token(" "),
				WORD.token("3")
				));

		assertThat(ct("a = [1,\\\n2]"), contains(
				WORD.token("a"),
				WS.token(" "),
				OP.token("="),
				WS.token(" "),
				BRACKET.token("["),
				WORD.token("1"),
				COMMA.token(","),
				WS.token("\\\n"),
				WORD.token("2"),
				BRACKET.token("]")
				));
	}

	@Test
	public void lineContinuationInString() {
		var command = "a = 'one line\\\nsecond line'";
		assertThat(ct(command), contains(
				WORD.token("a"),
				WS.token(" "),
				OP.token("="),
				WS.token(" "),
				STRING.token("'one line\\\nsecond line'")));
	}
	private static List<Token> ct(String command) {
		return new CommandTokenizer(command).tokens();
	}
}
