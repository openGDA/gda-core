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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

public class TokenStreanTranslatorTest {
	private Translator translator = new TokenStreamTranslator();

	//====================================================================
	// Valid Jython
	//====================================================================
	@Test
	public void noArgAlias() {
		setAliases("pos");
		assertThat(tr("pos"), is("pos()"));
		assertThat(tr("pos "), is("pos()"));
		assertThat(tr("nonAlias"), is("nonAlias"));
		assertThat(tr("\t"), is("\t"));
	}

	@Test
	public void basicAliasCommand() {
		setAliases("pos");
		assertThat(tr("pos foo '12'"), is("pos(foo, '12')"));
		assertThat(tr("pos foo 12"), is("pos(foo, 12)"));
	}

	@Test
	public void normalFunctionDefinition() {
		var func = "def foo():\n"
				+ "\treturn 'bar'\n";
		assertThat(tr(func), is(func));
	}

	@Test
	public void functionCallingAlias() {
		setAliases("pos");
		assertThat(tr("def foo():\n\tpos abc 12\n"), is("def foo():\n\tpos(abc, 12)\n"));
	}

	@Test
	public void aliasWithComment() {
		setAliases("pos");
		assertThat(tr("pos foo 1 # comment"), is("pos(foo, 1)# comment"));
		assertThat(tr("pos foo 1# comment"), is("pos(foo, 1)# comment"));
		assertThat(tr("pos foo 1#comment"), is("pos(foo, 1)#comment"));
		assertThat(tr("pos foo 1 # comment\n"), is("pos(foo, 1)# comment\n"));
	}

	@Test
	public void aliasCalledAsFunction() {
		setAliases("pos");
		var command = "pos(foo, 12)";
		// pos() should be treated as if it hadn't been aliased
		assertThat(tr("pos()"), is("pos()"));
		assertThat(tr("pos  ()"), is("pos()"));
		// pos(foo, 1) should be treated as a function call
		assertThat(tr(command), is(command));
		// pos (foo, 1) should be treated as passing (foo, 1) to pos
		assertThat(tr("pos (foo, 12)"), is("pos(foo, 12)"));
		// pos (foo, 1) bar should be treated as passing (foo, 1) and bar to pos
		assertThat(tr("pos (foo, 12) bar"), is("pos((foo, 12), bar)"));
		setAliases("scan");
		// example from DAQ-831
		assertThat(tr("scan(DummyScannable('ds'), 0, 0, 1, det, 5)"), is("scan(DummyScannable('ds'), 0, 0, 1, det, 5)"));
	}

	@Test
	public void keywordArgsInAlias() {
		// From example in DAQ-831
		setAliases("scan");
		assertThat(tr("scan x 0 10 1 *get_scannables(foo=bar)"), is("scan(x, 0, 10, 1, *get_scannables(foo=bar))"));
	}

	@Test
	public void aliasCalledWithCommas() {
		setAliases("pos");
		assertThat(tr("pos foo, 23"), is("pos(foo, 23)"));
		assertThat(tr("pos foo,bar"), is("pos(foo, bar)"));
		assertThat(tr("pos foo,(bar,32)"), is("pos(foo, (bar,32))"));
	}

	@Test
	public void multilineSingleCommand() {
		setAliases("pos");
		assertThat(tr("pos foo '''one\ntwo'''"), is("pos(foo, '''one\ntwo''')"));
	}

	@Test
	public void aliasInMultiline() {
		setAliases("pos");
		var command = "'''one\npos foo 23\ntwo'''";
		assertThat(tr(command), is(command));
	}

	@Test
	public void commentInMultiline() {
		var command = "a = ( # comment\n\t2,\n\t3\n)";
		assertThat(tr(command), is(command));
	}

	@Test
	public void commentInMultilineAlias() {
		setAliases("foo");
		assertThat(tr("foo 12 ( # comment\n\t2,\n\t3\n)"), is("foo(12, ( # comment\n\t2,\n\t3\n))"));
	}

	@Test
	public void semicolonSplitCommands() {
		String commands = "print(abcd); print(efgh)";
		assertThat(tr(commands), is(commands));

		setAliases("pos");
		assertThat(tr("pos foo 1; pos bar 2"), is("pos(foo, 1); pos(bar, 2)"));
	}

	@Test
	public void semicolonInString() {
		var command = "print 'one; two'";
		assertThat(tr(command), is(command));
	}

	@Test
	public void multilineCommand() {
		assertThat(tr("a = (1, 2\n3, 4)"), is("a = (1, 2\n3, 4)"));
	}

	@Test
	public void multilineAliasedCommand() {
		setAliases("pos");
		assertThat(tr("pos foo (1, \n2, 3)"), is("pos(foo, (1, \n2, 3))"));
	}

	@Test
	public void varargAlias() {
		setVarargAliases("foo");
		assertThat(tr("foo 1 2 3"), is("foo([1, 2, 3])"));
		// TODO: This is validating the 'incorrect' behaviour of the old translator
		// that is relied on until the Jython vararg bug (#100) is fixed.
		assertThat(tr("foo"), is("foo()"));
	}

	@Test
	public void commasInAlias() {
		// Commas aren't required but don't need extra commas to be added
		setAliases("foo");
		assertThat(tr("foo one, two, three"), is("foo(one, two, three)"));
		assertThat(tr("foo one, two three"), is("foo(one, two, three)"));
		// Too many commas will still fail to run but they should still be wrapped
		assertThat(tr("foo one, , two"), is("foo(one, , two)"));
	}

	@Test
	public void extraWhitespace() {
		setAliases("foo");
		assertThat(tr("foo 1   ,   3"), is("foo(1, 3)"));
		assertThat(tr("foo(2  ,  4)"), is("foo(2  ,  4)"));
		assertThat(tr("foo( 2  ,  4 )"), is("foo( 2  ,  4 )"));
		assertThat(tr("foo 2    3"), is("foo(2, 3)"));
		// The next couple aren't valid python but should be wrapped anyway
		assertThat(tr("foo 2 ,  , 3"), is("foo(2, , 3)"));
		assertThat(tr("foo 2 ,, 3"), is("foo(2, , 3)"));
	}

	@Test
	public void lineContinuation() {
		assertThat(tr("a = 'one line\\\nsecond line'"), is("a = 'one line\\\nsecond line'"));
	}

	@Test
	public void multipleCommands() {
		var command = "print(one)\nprint(two)";
		assertThat(tr(command), is(command));

		var spacedCommand = "print(three)\n\nprint(four)\n\nprint(five)";
		assertThat(tr(spacedCommand), is(spacedCommand));

		setAliases("alias");
		var withAliasAsFunction = "rscan = gdascans.Rscan()\n"
				+ "alias('rscan')\n"
				+ "print(rscan.__doc__.split('\n')[2])";
		assertThat(tr(withAliasAsFunction), is(withAliasAsFunction));
	}

	@Test
	public void multipleAliasCalls() {
		setAliases("foo");
		setVarargAliases("bar");
		var multipleAliasCalls = "print('not an alias')\n"
				+ "foo one two\n"
				+ "bar three four five\n"
				+ "foo six # and a comment\n";
		var expected = "print('not an alias')\n"
				+ "foo(one, two)\n"
				+ "bar([three, four, five])\n"
				+ "foo(six)# and a comment\n";
		assertThat(tr(multipleAliasCalls), is(expected));
	}

	//====================================================================
	// Invalid Jython - should fail in a way that let's error be useful
	//====================================================================

	@Test
	public void unclosedString() {
		assertThat(tr("'partial string"), is("'partial string"));
	}

	@Test
	public void mismatchedBrackets() {
		setAliases("pos");
		assertThat(tr("pos foo [1, 2, 3, 4)"), is("pos(foo, [1, 2, 3, 4))"));
	}

	@Test
	public void doubleComma() {
		setAliases("foo");
		assertThat(tr("foo 1, , 3"), is("foo(1, , 3)"));
	}


	//====================================================================
	// Regression tests - bugs that have been seen before
	//====================================================================

	@Test
	// DAQ-3549
	public void dictLookupsInAlias() {
		setAliases("pos");
		assertThat(tr("pos x d['a']['b']"), is("pos(x, d['a']['b'])"));
	}

	@Test
	// DAQ-3549
	public void functionCallInAliasAsFunction() {
		setAliases("pos");
		var command = "pos(ix, finder.find(\"iy\").getPosition())";
		assertThat(tr(command), is(command));
	}

	@Test
	// DAQ-3549
	public void functionCallInAlias() {
		setAliases("pos");
		var command = "pos ix finder.find(\"iy\").getPosition()";
		assertThat(tr(command), is("pos(ix, finder.find(\"iy\").getPosition())"));
	}

	@Test
	// DAQ-831
	public void listOfDicts() {
		var command = "a = [{'row': 1, 'column': 2}, {'row':2, 'column':2}]";
		assertThat(tr(command), is(command));
	}

	@Test
	// DAQ-831
	public void listOfLambdas() {
		var command = "a = [lambda a: a+2, lambda b: b+3]";
		assertThat(tr(command), is(command));
	}

	@Test
	// DAQ-831
	public void commentInIfBlock() {
		var command = "if 1:\n\tpass\n\t#comment\nelse:\n\tpass\n";
		assertThat(tr(command), is(command));
	}

	@Test
	// DAQ-831
	public void functionOverManyLines() {
		var command = "def foo():\n\tbar(\n\t\tx = y\n\t)";
		assertThat(tr(command), is(command));
	}

	@Test
	// DAQ-831
	public void singleElementTuple() {
		setAliases("foo");
		var command = "foo (1,) 1";
		assertThat(tr(command), is("foo((1,), 1)"));
	}

	@Test
	// DAQ-831
	public void listToAliasedFunction() {
		setAliases("pos");
		var command = "pos bm0 ([10] + [0]*11)";
		assertThat(tr(command), is("pos(bm0, ([10] + [0]*11))"));
	}

	@Test
	// DAQ-831
	public void multipleSemicolons() {
		var command = "print 'one;;; two'";
		assertThat(tr(command), is(command));
	}

	@Test
	// DAQ-831
	public void semicolonInComment() {
		var command = "foo # ;bar";
		assertThat(tr(command), is(command));
	}

	private String tr(String command) {
		return translator.translate(command);
	}

	private void setAliases(String... aliases) {
		Stream.of(aliases).forEach(translator::addAliasedCommand);
	}

	private void setVarargAliases(String... aliases) {
		Stream.of(aliases).forEach(translator::addAliasedVarargCommand);
	}
}
