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

package gda.jython;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.python.core.Py;
import org.python.core.PyObject;

import gda.factory.Findable;

public class GdaBuiltinTest {

	@Test
	public void primitiveDocString() {
		GdaBuiltin bar = builtinFor(Commands.class, "bar");
		String docs = "bar(int) -> long\n"
				+ "    Bar an integer\n"
				+ "\n"
				+ "from gda.jython.GdaBuiltinTest.Commands";
		assertThat(bar.__getattr__("__doc__").toString(), is(docs));
	}

	@Test
	public void testOverloadedDocString() throws Exception {
		PyObject foo = builtinFor(Commands.class, "foo");
		String docs = "foo(String)\n" +
				"    Foo a single String\n" +
				"foo(String, String) -> String\n" +
				"    Foo two strings into one\n"
				+ "\n"
				+ "from gda.jython.GdaBuiltinTest.Commands";
		assertThat(foo.__getattr__("__doc__").toString(), is(docs));
	}

	@Test
	public void testGenericDocString() {
		GdaBuiltin awkward = builtinFor(Commands.class, "awkward");
		String docs = "awkward(Map<String, Set<? extends Findable>>, List<String>[], Class<?>) -> List<String[]>\n"
				+ "    Awkward types\n"
				+ "\n"
				+ "from gda.jython.GdaBuiltinTest.Commands";
		assertThat(awkward.__getattr__("__doc__").toString(), is(docs));
	}

	@Test
	public void testVarargDocString() {
		GdaBuiltin vararg = builtinFor(Commands.class, "varargs");
		String docs = "varargs(String...)\n"
				+ "    Method with variable Strings\n"
				+ "\n"
				+ "from gda.jython.GdaBuiltinTest.Commands";
		assertThat(vararg.__getattr__("__doc__").toString(), is(docs));
	}

	@Test
	public void testTypeParameterDocString() {
		GdaBuiltin typed = builtinFor(Commands.class, "typed");
		String docs = "typed(S) -> T\n"
				+ "    where: S is any type\n"
				+ "           T extends Findable\n"
				+ "    Type variables\n"
				+ "\n"
				+ "from gda.jython.GdaBuiltinTest.Commands";
		assertThat(typed.__getattr__("__doc__").toString(), is(docs));
	}

	@Test
	public void builtinHasCorrectName() throws Exception {
		assertThat(builtinFor(Commands.class, "typed"), is(named("typed")));
	}

	@Test
	public void correctFunctionIsCalled() throws Exception {
		GdaBuiltin foo = builtinFor(Commands.class, "foo");
		PyObject first = foo.__call__(Py.java2py("helloWorld"));
		assertThat(first, is(Py.None)); // void methods return None

		PyObject second = foo.__call__(Py.javas2pys("hello", "World"));
		assertThat(second, is(Py.java2py("helloWorld"))); // return as jython object
	}

	@Test(expected = IllegalArgumentException.class)
	public void methodlessBuiltinThrows() {
		builtinFor(Commands.class, "missing");
	}

	@Test
	public void methodlessClassReturnsEmptyMap() {
		Collection<GdaBuiltin> builtins = GdaBuiltin.builtinMethodsFrom(Methodless.class);
		assertThat(builtins, is(empty()));
	}

	@Test
	public void notStaticMethodsAreIgnored() {
		Collection<GdaBuiltin> builtins = GdaBuiltin.builtinMethodsFrom(Commands.class);
		assertThat(builtins, does(not(hasItem(named("nonStatic")))));
	}

	@Test
	public void methodsNeedAnnotations() {
		Collection<GdaBuiltin> builtins = GdaBuiltin.builtinMethodsFrom(Commands.class);
		assertThat(builtins, does(not(hasItem(named("ignored")))));
	}

	private GdaBuiltin builtinFor(Class<?> clazz, String name) {
		List<Method> methods = Stream.of(clazz.getMethods())
				.filter(m -> name.equals(m.getName()))
				.collect(toList());
		return new GdaBuiltin(name, methods);
	}

	/** Create a Matcher that checks the name of a GdaBuiltin, the methods used are ignored */
	private Matcher<GdaBuiltin> named(String name) {
		return new FeatureMatcher<GdaBuiltin, String>(equalTo(name), "Builtin name should be", "name") {
			@Override
			protected String featureValueOf(GdaBuiltin actual) {
				return actual.getName();
			}
		};
	}

	/** Inner class with static methods */
	public static class Commands {
		@GdaJythonBuiltin("Foo a single String")
		public static void foo(@SuppressWarnings("unused") String a) {}

		@GdaJythonBuiltin("Foo two strings into one")
		public static String foo(String one, String two) {
			return one + two;
		}

		@GdaJythonBuiltin("Method with variable Strings")
		public static void varargs(@SuppressWarnings("unused") String...strings) {}

		@GdaJythonBuiltin("Bar an integer")
		public static long bar(int j) {
			return j << 6;
		}

		@GdaJythonBuiltin("Awkward types")
		public static List<String[]> awkward(
				@SuppressWarnings("unused") Map<String, Set<? extends Findable>> map,
				@SuppressWarnings("unused") List<String>[] strings,
				@SuppressWarnings("unused") Class<?> clazz
				) {
			return emptyList();
		}

		@GdaJythonBuiltin("Type variables")
		public static <T extends Findable, S> T typed(@SuppressWarnings("unused") S foo) {
			return null;
		}

		/** Method without annotation */
		public static void ignored() {}

		@GdaJythonBuiltin("Should be ignored")
		public void nonStatic() {}
	}
	/** Inner class without static methods */
	public static class Methodless {}

	/** Pointless redirect method to make assertions read more fluently */
	private static <T> Matcher<T> does(Matcher<T> matcher) {
		return is(matcher);
	}
}
