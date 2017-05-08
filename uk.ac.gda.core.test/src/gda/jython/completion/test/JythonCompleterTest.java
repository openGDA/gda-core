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

package gda.jython.completion.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PyMethod;
import org.python.core.PyString;
import org.python.core.PyTuple;

import gda.factory.FactoryException;
import gda.jython.Jython;
import gda.jython.JythonServer;
import gda.jython.completion.AutoCompletion;
import gda.jython.completion.impl.JythonCompleter;

public class JythonCompleterTest {

	private JythonCompleter jc;
	private Jython jy;
	private PyMethod jyFunc;

	@Before
	public void setup() throws FactoryException {
		jy = PowerMockito.mock(JythonServer.class);
		jyFunc = PowerMockito.mock(PyMethod.class);
		Mockito.when(jy.eval("Completer(globals()).complete")).thenReturn(jyFunc);
		Mockito.when(jyFunc.__call__(new PyString(""))).thenReturn(makeList(makeTuple("abs", 2), makeTuple("pos", 2)));

		jc = new JythonCompleter(jy);
	}

	@Test
	public void testEmptyCompletionWithCompleteOnEmpty() {
		jc.setCompleteOnEmpty(true);
		Collection<String> expectedGlobals = Arrays.asList(new String[] {"abs", "pos"});
		Collection<String> actualGlobals = jc.getCompletionsFor("", 0).getStrings();

		assertEquals("Expected globals not present", expectedGlobals, actualGlobals);
	}

	@Test
	public void testEmptyCompletionWithoutCompleteOnEmpty() {
		Collection<String> expectedGlobals = Arrays.asList(new String[] {"\t"});
		Collection<String> actualGlobals = jc.getCompletionsFor("", 0).getStrings();

		assertEquals("Expected globals not present", expectedGlobals, actualGlobals);
	}

	@Test
	public void testTabCompletionWithCompleteOnEmpty() {
		jc.setCompleteOnEmpty(true);
		Collection<String> expectedGlobals = Arrays.asList(new String[] {"abs", "pos"});
		Collection<String> actualGlobals = jc.getCompletionsFor("\t", 1).getStrings();
		assertEquals("Expected globals not present", expectedGlobals, actualGlobals);
	}

	@Test
	public void testTabCompletionWithoutCompleteOnEmpty() {
		Collection<String> expectedGlobals = Arrays.asList(new String[] {"\t\t"});
		Collection<String> actualGlobals = jc.getCompletionsFor("\t", 1).getStrings();
		assertEquals("Expected globals not present", expectedGlobals, actualGlobals);
	}

	@Test
	public void testSpaceCompletionWithCompleteOnEmpty() {
		jc.setCompleteOnEmpty(true);
		Collection<String> expectedGlobals = Arrays.asList(new String[] {"abs", "pos"});
		Collection<String> actualGlobals = jc.getCompletionsFor(" ", 1).getStrings();

		assertEquals("Expected globals not present", expectedGlobals, actualGlobals);
	}

	@Test
	public void testSpaceCompletionWithoutCompleteOnEmpty() {
		Collection<String> expectedGlobals = Arrays.asList(new String[] {});
		Collection<String> actualGlobals = jc.getCompletionsFor(" ", 1).getStrings();
		assertEquals("Expected globals not present", expectedGlobals, actualGlobals);
	}

	@Test
	public void testGlobalCompletion() {
		Mockito.when(jyFunc.__call__(new PyString("a"))).thenReturn(makeList(makeTuple("abs", 2)));
		Collection<String> expectedGlobals = Arrays.asList(new String[] {"abs"});
		Collection<Integer> expectedTypes = Arrays.asList(new Integer[] {2});
		AutoCompletion comp = jc.getCompletionsFor("a", 1);
		Collection<String> actualGlobals = comp.getStrings();
		Collection<Integer> actualTypes = comp.getOptions().stream().map(opt -> opt.type.ordinal()).collect(Collectors.toList());

		assertEquals("Expected globals not present", expectedGlobals, actualGlobals);
		assertEquals("Expected globals not correct types", expectedTypes, actualTypes);
	}

	@Test
	public void testMethodCompletion() {
		Mockito.when(jyFunc.__call__(new PyString("dummy_x.get"))).thenReturn(makeList(makeTuple("getPostion", 2), makeTuple("getBusy", 2)));
		Collection<String> expectedMethods = Arrays.asList(new String[] {"getPostion", "getBusy"});
		Collection<Integer> expectedTypes = Arrays.asList(new Integer[] {2, 2});

		AutoCompletion comp = jc.getCompletionsFor("\tprint(dummy_x.get)", 18);
		Collection<String> actualMethods = comp.getStrings();
		Collection<Integer> actualTypes = comp.getOptions().stream().map(opt -> opt.type.ordinal()).collect(Collectors.toList());

		assertEquals("Expected methods not present", expectedMethods, actualMethods);
		assertEquals("Methods have wrong types", expectedTypes, actualTypes);
		assertEquals("Incorrect string before completions", "\tprint(dummy_x.", comp.getBefore());
		assertEquals("Incorrect string after completion", ")", comp.getAfter());
		assertEquals("Incorrect position after completion", 15, comp.getPosition());
	}

	@Test
	public void testGetAllMethods() {
		Mockito.when(jyFunc.__call__(new PyString("dummy_x."))).thenReturn(makeList(
				makeTuple("getPostion", 2),
				makeTuple("isBusy", 2),
				makeTuple("position", 3)));
		Collection<String> expectedMethods = Arrays.asList(new String[] {"getPostion", "isBusy", "position"});
		Collection<Integer> expectedTypes = Arrays.asList(new Integer[] {2, 2, 3});

		AutoCompletion comp = jc.getCompletionsFor("\tdummy_x.", 9);
		Collection<String> actualMethods = comp.getStrings();
		Collection<Integer> actualTypes = comp.getOptions().stream().map(opt -> opt.type.ordinal()).collect(Collectors.toList());

		assertEquals("Expected methods not present", expectedMethods, actualMethods);
		assertEquals("Methods have wrong types", expectedTypes, actualTypes);
		assertEquals("Incorrect string before completions", "\tdummy_x.", comp.getBefore());
		assertEquals("Incorrect string after completion", "", comp.getAfter());
		assertEquals("Incorrect position after completion", 9, comp.getPosition());
	}

	private PyList makeList(Object...objs) {
		Collection<Object> pylist = new ArrayList<>();
		for (Object obj : objs) {
			pylist.add(obj);
		}
		return new PyList(pylist);
	}

	private PyTuple makeTuple(String comp, int type) {
		return new PyTuple(
				new PyString(comp),
				new PyString(""),
				new PyString(""),
				new PyInteger(type));
	}
}
