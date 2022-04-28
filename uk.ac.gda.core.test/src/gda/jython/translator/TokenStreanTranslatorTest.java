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

class TokenStreanTranslatorTest {
	private Translator translator = new TokenStreamTranslator();

	//====================================================================
	// Valid Jython
	//====================================================================
	@Test
	void noArgAlias() {
		setAliases("pos");
		assertThat(tr("pos"), is("pos()"));
		assertThat(tr("pos "), is("pos() "));
		assertThat(tr("nonAlias"), is("nonAlias"));
		assertThat(tr("\t"), is("\t"));
	}

	@Test
	void basicAliasCommand() {
		setAliases("pos");
		assertThat(tr("pos foo '12'"), is("pos(foo, '12')"));
		assertThat(tr("pos foo 12"), is("pos(foo, 12)"));
	}

	@Test
	void tupleToAliasFunction() throws Exception {
		setAliases("pos");
		assertThat(tr("pos  ()"), is("pos(())"));
		// pos (foo, 1) should be treated as passing (foo, 1) to pos
		assertThat(tr("pos (foo, 12)"), is("pos((foo, 12))"));
		// pos (foo, 1) bar should be treated as passing (foo, 1) and bar to pos
		assertThat(tr("pos (foo, 12) bar"), is("pos((foo, 12), bar)"));
	}

	@Test
	void emptyString() throws Exception {
		assertThat(tr(""), is(""));
	}

	@Test
	void normalFunctionDefinition() {
		var func = "def foo():\n"
				+ "\treturn 'bar'\n";
		assertThat(tr(func), is(func));
	}

	@Test
	void functionCallingAlias() {
		setAliases("pos");
		assertThat(tr("def foo():\n\tpos abc 12\n"), is("def foo():\n\tpos(abc, 12)\n"));
	}

	@Test
	void aliasWithComment() {
		setAliases("pos");
		assertThat(tr("pos foo 1 # comment"), is("pos(foo, 1) # comment"));
		assertThat(tr("pos foo 1# comment"), is("pos(foo, 1)# comment"));
		assertThat(tr("pos foo 1#comment"), is("pos(foo, 1)#comment"));
		assertThat(tr("pos foo 1 # comment\n"), is("pos(foo, 1) # comment\n"));
	}

	@Test
	void aliasCalledAsFunction() {
		setAliases("pos");
		// pos() should be treated as if it hadn't been aliased
		assertThat(tr("pos()"), is("pos()"));
		var command = "pos(foo, 12)";
		// pos(foo, 1) should be treated as a function call
		assertThat(tr(command), is(command));
		setAliases("scan");
		// example from DAQ-831
		assertThat(tr("scan(DummyScannable('ds'), 0, 0, 1, det, 5)"), is("scan(DummyScannable('ds'), 0, 0, 1, det, 5)"));
	}

	@Test
	void keywordArgsInAlias() {
		// From example in DAQ-831
		setAliases("scan");
		assertThat(tr("scan x 0 10 1 *get_scannables(foo=bar)"), is("scan(x, 0, 10, 1, *get_scannables(foo=bar))"));
	}

	@Test
	void aliasCalledWithCommas() {
		setAliases("pos");
		assertThat(tr("pos foo, 23"), is("pos(foo, 23)"));
		assertThat(tr("pos foo,bar"), is("pos(foo,bar)"));
		assertThat(tr("pos foo,(bar,32)"), is("pos(foo,(bar,32))"));
	}

	@Test
	void multilineSingleCommand() {
		setAliases("pos");
		assertThat(tr("pos foo '''one\ntwo'''"), is("pos(foo, '''one\ntwo''')"));
	}

	@Test
	void aliasInMultiline() {
		setAliases("pos");
		var command = "'''one\npos foo 23\ntwo'''";
		assertThat(tr(command), is(command));
	}

	@Test
	void commentInMultiline() {
		var command = "a = ( # comment\n\t2,\n\t3\n)";
		assertThat(tr(command), is(command));
	}

	@Test
	void commentInEmptyAlias() throws Exception {
		setAliases("demo");
		assertThat(tr("demo # should expand alias"), is("demo() # should expand alias"));
	}

	@Test
	void commentInMultilineAlias() {
		setAliases("foo");
		var raw = """
				foo 12 [ # comment
					2,# comment
					3
				]
				""";
		var exp = """
				foo(12, [ # comment
					2,# comment
					3
				])
				""";
		assertThat(tr(raw), is(exp));
	}

	@Test
	void splitIndexing() throws Exception {
		// without being in an alias
		var line = "abcd[\n\t1,\n\t2,\n]";
		assertThat(tr(line), is(line));

		// as argument to alias
		setAliases("demo");
		line = "demo abcd[\n\t1,\n\t2\n]";
		var exp = "demo(abcd[\n\t1,\n\t2\n])";
		assertThat(tr(line), is(exp));
	}

	@Test
	void indexingInAlias() throws Exception {
		setAliases("demo");
		assertThat(tr("demo foo[1,2]"), is("demo(foo[1,2])"));
	}

	@Test
	void semicolonSplitCommands() {
		String commands = "print(abcd); print(efgh)";
		assertThat(tr(commands), is(commands));

		setAliases("pos");
		assertThat(tr("pos foo 1.2; pos bar 2"), is("pos(foo, 1.2); pos(bar, 2)"));
	}

	@Test
	void semicolonInString() {
		var command = "print 'one; two'";
		assertThat(tr(command), is(command));
	}

	@Test
	void multilineCommand() {
		assertThat(tr("a = (1, 2\n3, 4)"), is("a = (1, 2\n3, 4)"));
	}

	@Test
	void multilineAliasedCommand() {
		setAliases("pos");
		assertThat(tr("pos foo (1, \n2, 3)"), is("pos(foo, (1, \n2, 3))"));
	}

	@Test
	void varargAlias() {
		setVarargAliases("foo");
		assertThat(tr("foo 1 2.12 3"), is("foo([1, 2.12, 3])"));
		// TODO: This is validating the 'incorrect' behaviour of the old translator
		// that is relied on until the Jython vararg bug (#100) is fixed.
		assertThat(tr("foo"), is("foo()"));
	}

	@Test
	void commasInAlias() {
		// Commas aren't required but don't need extra commas to be added
		setAliases("foo");
		assertThat(tr("foo one, two, three"), is("foo(one, two, three)"));
		assertThat(tr("foo one, two three"), is("foo(one, two, three)"));
		// Too many commas will still fail to run but they should still be wrapped
		assertThat(tr("foo one, , two"), is("foo(one, , two)"));
	}

	@Test
	void extraWhitespace() {
		setAliases("foo");
		assertThat(tr("foo 1.0   ,   3"), is("foo(1.0   ,   3)"));
		assertThat(tr("foo(2  ,  4.0)"), is("foo(2  ,  4.0)"));
		assertThat(tr("foo( 2  ,  4 )"), is("foo( 2  ,  4 )"));
		assertThat(tr("foo 2    3"), is("foo(2,    3)"));
		// The next couple aren't valid python but should be wrapped anyway
		assertThat(tr("foo 2 ,  , 3"), is("foo(2 ,  , 3)"));
		assertThat(tr("foo 2 ,, 3"), is("foo(2 ,, 3)"));
	}

	@Test
	void lineContinuation() {
		assertThat(tr("a = 'one line\\\nsecond line'"), is("a = 'one line\\\nsecond line'"));
	}

	@Test
	void multipleCommands() {
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
	void multipleAliasCalls() {
		setAliases("foo");
		setVarargAliases("bar");
		var multipleAliasCalls = "print('not an alias')\n"
				+ "foo one two\n"
				+ "bar three four five\n"
				+ "foo six # and a comment\n";
		var expected = "print('not an alias')\n"
				+ "foo(one, two)\n"
				+ "bar([three, four, five])\n"
				+ "foo(six) # and a comment\n";
		assertThat(tr(multipleAliasCalls), is(expected));
	}

	@Test
	void extendedArrays() throws Exception {
		// Outside alias calls, arrays are untouched
		assertThat(tr("[]"), is("[]"));
		assertThat(tr("[1]"), is("[1]"));
		assertThat(tr("[1 2]"), is("[1 2]"));

		setAliases("demo");
		assertThat(tr("demo [1 2 3]"), is("demo([1, 2, 3])"));
		assertThat(tr("demo [1 [2 3] 4]"), is("demo([1, [2, 3], 4])"));

		assertThat(tr("demo [1 -2]"), is("demo([1, -2])"));
		assertThat(tr("demo [1-2]"), is("demo([1-2])"));
		assertThat(tr("demo [1 - 2]"), is("demo([1, - 2])"));
	}

	@Test
	void commasInLists() throws Exception {
		// Outside aliases, arrays should be untouched
		assertThat(tr("[1, 2, 3]"), is("[1, 2, 3]"));

		// Within alias calls, normal arrays should be left
		setAliases("demo");
		assertThat(tr("demo [1, 2, 3]"), is("demo([1, 2, 3])"));
		assertThat(tr("demo [1, 2, 3] [4 5 6]"), is("demo([1, 2, 3], [4, 5, 6])"));
	}

	@Test
	void escapedExtendedArrays() throws Exception {
		setAliases("demo");
		// Nothing inside () is modified, this allows for complex patterns to be escaped
		assertThat(tr("demo [1 2 (3 [4 5] 6) 7]"), is("demo([1, 2, (3 [4 5] 6), 7])"));
		assertThat(
				tr("demo [(lambda x, y: x - y) ([i for i in range(10) if i % 2])]"),
				is("demo([(lambda x, y: x - y), ([i for i in range(10) if i % 2])])"));
	}

	@Test
	void mixedCommasInArrays() throws Exception {
		setAliases("demo");
		// Some missing commas
		assertThat(tr("demo [1, 2 3]"), is("demo([1, 2, 3])"));
		// Commas in outer list, not in inner
		assertThat(tr("demo [1, [2 3], 4]"), is("demo([1, [2, 3], 4])"));
		// Commas in inner list, not in outer
		assertThat(tr("demo [1 [2, 3] 4]"), is("demo([1, [2, 3], 4])"));
	}

	@Test
	void ambiguousExtendedArrays() throws Exception {
		setAliases("demo");
		// With WS elements should be interpreted as separate elements
		assertThat(tr("demo [abcd [12] (4, 5)]"), is("demo([abcd, [12], (4, 5)])"));
		// Without spaces, the elements should not be treated as separate
		assertThat(tr("demo [abcd[12](4, 5)]"), is("demo([abcd[12](4, 5)])"));
	}

	//====================================================================
	// Invalid Jython - should fail in a way that lets error be useful
	//====================================================================

	@Test
	void unclosedString() {
		assertThat(tr("'partial string"), is("'partial string"));
	}

	@Test
	void aliasMissingClosingBracket() throws Exception {
		setAliases("demo");
		assertThat(tr("demo (1, 2, 3"), is("demo((1, 2, 3)"));
	}

	@Test
	void mismatchedBrackets() {
		setAliases("pos");
		assertThat(tr("pos foo [1, 2, 3, 4)"), is("pos(foo, [1, 2, 3, 4))"));
	}

	@Test
	void doubleComma() {
		setAliases("foo");
		assertThat(tr("foo 1, , 3"), is("foo(1, , 3)"));
	}


	//====================================================================
	// Regression tests - bugs that have been seen before
	//====================================================================

	@Test
	// DAQ-3549
	void dictLookupsInAlias() {
		setAliases("pos");
		assertThat(tr("pos x d['a']['b']"), is("pos(x, d['a']['b'])"));
	}

	@Test
	// DAQ-3549
	void functionCallInAliasAsFunction() {
		setAliases("pos");
		var command = "pos(ix, finder.find(\"iy\").getPosition())";
		assertThat(tr(command), is(command));
	}

	@Test
	// DAQ-3549
	void functionCallInAlias() {
		setAliases("pos");
		var command = "pos ix finder.find(\"iy\").getPosition()";
		assertThat(tr(command), is("pos(ix, finder.find(\"iy\").getPosition())"));
	}

	@Test
	// DAQ-831
	void listOfDicts() {
		var command = "a = [{'row': 1, 'column': 2}, {'row':2, 'column':2}]";
		assertThat(tr(command), is(command));
	}

	@Test
	// DAQ-831
	void listOfLambdas() {
		var command = "a = [lambda a: a+2, lambda b: b+3]";
		assertThat(tr(command), is(command));
	}

	@Test
	// DAQ-831
	void commentInIfBlock() {
		var command = "if 1:\n\tpass\n\t#comment\nelse:\n\tpass\n";
		assertThat(tr(command), is(command));
	}

	@Test
	// DAQ-831
	void functionOverManyLines() {
		var command = "def foo():\n\tbar(\n\t\tx = y\n\t)";
		assertThat(tr(command), is(command));
	}

	@Test
	// DAQ-831
	void singleElementTuple() {
		setAliases("foo");
		assertThat(tr("foo (1,) 1"), is("foo((1,), 1)"));
		assertThat(tr("foo (1,)"), is("foo((1,))"));
	}

	@Test
	// DAQ-831
	void listToAliasedFunction() {
		setAliases("pos");
		var command = "pos bm0 ([10] + [0]*11)";
		assertThat(tr(command), is("pos(bm0, ([10] + [0]*11))"));
	}

	@Test
	// DAQ-831
	void multipleSemicolons() {
		var command = "print 'one;;; two'";
		assertThat(tr(command), is(command));
	}

	@Test
	// DAQ-831
	void semicolonInComment() {
		var command = "foo # ;bar";
		assertThat(tr(command), is(command));
	}

	@Test
	// I18-551
	void callMethodOnAlias() {
		setAliases("foo");
		assertThat(tr("foo.bar()"), is("foo.bar()"));
	}

	@Test
	// I18-551
	void accessAttributeOnAlias() {
		setAliases("foo");

		// accessing attributes shouldn't be affected
		assertThat(tr("foo.bar"), is("foo.bar"));
		assertThat(tr("foo.bar = False"), is("foo.bar = False"));

		// passing numbers to aliases should still work
		assertThat(tr("foo .3"), is("foo(.3)"));
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
