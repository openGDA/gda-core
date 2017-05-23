/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.osgi.framework.Constants.EXPORT_PACKAGE;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;


/**
 * Can't use direct type comparison in these tests as we would necessarily have to load the Classes to compare to those loaded by
 * the ClassLoader under a test, using a different ClassLoader. This would mean that, in the eyes of all type comparison schemes
 * such as instanceof, isAssignableFrom etc., the types would always be different and the tests would always fail. The only reliable
 * way to compare instances of a class loaded by different ClassLoaders is piecemeal using reflection as below.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(LoggerFactory.class)
public class GDAJythonClassloaderTest {

	private static final String PKG_1 = "bbc.python.monty.lumberjack";
	private static final String PKG_2 = "bbc.python.monty.gumby";
	private static final String PKG_3 = "bbc.python.monty.nudge.nudge";
	private static final String PKG_4 = "bbc.spam.spam.eggs.spam";
	private static final String FOLDER_1 = "/tmp";
	private static final String BAD_CLASS_NAME = "NotActuallyAClass";
	private static final String GOOD_CLASS_NAME = "bbc.python.monty.nudge.nudge.SayNoMore";
	private static final String FILLER = "__Pystuff__";
	private static final String JAR_CLASS_NAME_ONE = "bbc.python.monty.twit.Nigel";
	private static final String JAR_CLASS_NAME_TWO = "bbc.python.monty.twit.Gervais";
	private static final String JAR_CLASS_NAME_THREE = "bbc.python.monty.twit.Race";
	private static final String JAR_NAME = "gda-core.git/uk.ac.gda.core.test/jars/test1.jar";
	private static final String JAR_LOCATION = "gda-core.git/uk.ac.gda.core.test/" + JAR_NAME;
	private static final String METHOD_NAME = "doSomethingUseless";
	private static final String METHOD_NAME_RACE = "getCompetitors";
	private static final String MAIN_CLASSLOADER_SUCCESS_MSG = "Loaded class {} from bundle {}";
	private static final String URL_CLASSLOADER_SUCCESS_MSG = "Loaded class {} from {}";
	private static final String NIGEL_RESPONSE = "Nigel doing something";
	private static final String GERVAIS_RESPONSE = "Gervais doing something";

	private static final String TEST_JAR_PATH = new File(JAR_NAME).getAbsolutePath();
	private static final String STD_SYS_PATH = String.format("[%s,%s]", FILLER, TEST_JAR_PATH);

	private final Map<String, Map<Bundle, Boolean>> included = new HashMap<>();
	private final Map<String, String> scriptFolders = new HashMap<String, String>(){{
			put(FOLDER_1, "TMP");
	}};

	private Bundle[] bundles;
	private Dictionary<String, String> headers1;
	private Dictionary<String, String> headers2;
	private Dictionary<String, String> headers3;

	private static Logger logger;

	private PyList sysPath;
	private GDAJythonClassLoader loader;

	private class DummyClass {
	}

	@BeforeClass
	public static void classSetup() {
		mockStatic(LoggerFactory.class);
		logger = mock(Logger.class);
		when(LoggerFactory.getLogger(GDAJythonClassLoader.class)).thenReturn(logger);
	}

	@SuppressWarnings("unchecked")
	@Before
	public void setup() throws Exception {
		Mockito.reset(logger);
		Bundle bundle1 = mock(Bundle.class);
		Bundle bundle2 = mock(Bundle.class);
		Bundle bundle3 = mock(Bundle.class);
		bundles = new Bundle[] {bundle1, bundle2, bundle3};
		headers1 = new Hashtable<String, String>(ImmutableMap.of(EXPORT_PACKAGE, PKG_1 + "," + PKG_2));
		headers2 = new Hashtable<String, String>(ImmutableMap.of(EXPORT_PACKAGE, PKG_4 + "," + PKG_2));
		headers3 = new Hashtable<String, String>(ImmutableMap.of(EXPORT_PACKAGE, PKG_3));
		included.put(PKG_1, new HashMap<>(ImmutableMap.of(bundle1, false)));
		included.put(PKG_2, new HashMap<>(ImmutableMap.of(bundle1, false, bundle2, false)));
		included.put(PKG_3, new HashMap<>(ImmutableMap.of(bundle3, false)));
		included.put(PKG_4, new HashMap<>(ImmutableMap.of(bundle2, true)));

		when(bundle1.getHeaders()).thenReturn(headers1);
		when(bundle2.getHeaders()).thenReturn(headers2);
		when(bundle3.getHeaders()).thenReturn(headers3);
		when(bundle1.getSymbolicName()).thenReturn("bundle1");
		when(bundle2.getSymbolicName()).thenReturn("bundle2");
		when(bundle3.getSymbolicName()).thenReturn("bundle3");
		when(bundle1.loadClass(BAD_CLASS_NAME)).thenThrow(ClassNotFoundException.class);
		when(bundle2.loadClass(BAD_CLASS_NAME)).thenThrow(ClassNotFoundException.class);
		when(bundle3.loadClass(BAD_CLASS_NAME)).thenThrow(ClassNotFoundException.class);
		Mockito.<Class<?>>when(bundle3.loadClass(GOOD_CLASS_NAME)).thenReturn(DummyClass.class);

		GDAJythonClassLoader.initialize(bundles, scriptFolders, included);
		loader = new GDAJythonClassLoader();
		sysPath = mock(PyList.class);
	}

	@After
	public void cleanup() {
		GDAJythonClassLoader.closeJarClassLoaders();
	}

	@Test
	public void initializationSucceeds() {
		assertThat(GDAJythonClassLoader.useGDAClassLoader(), equalTo(true));
	}

	@Test
	public void packagesAreSetAsInitialized() {
		assertThat(loader.getJythonPackages(), hasItems(PKG_1, PKG_2, PKG_3, PKG_4));
	}

	@Test
	public void pathsAreSetAsInitialized() {
		assertThat(loader.getStandardFolders().keySet(), hasItems(FOLDER_1));
	}

	@Test
	public void requiredPackageInclusionsAreSetAsInitialized() {
		assertThat(loader.isIncludedCombination(bundles[1], PKG_4), not(Optional.empty()));
		assertThat(loader.isIncludedCombination(bundles[1], PKG_4).get(), is(true));
	}

	@Test
	public void requiredPackageExclusionsAreSetAsInitialized() {
		assertThat(loader.isIncludedCombination(bundles[1], PKG_4), not(Optional.empty()));
		assertThat(loader.isIncludedCombination(bundles[0], PKG_1).get(), is(false));
		assertThat(loader.isIncludedCombination(bundles[0], PKG_2).get(), is(false));
		assertThat(loader.isIncludedCombination(bundles[1], PKG_2).get(), is(false));
		assertThat(loader.isIncludedCombination(bundles[2], PKG_3).get(), is(false));
	}

	@Test
	public void unknownPackagesHaveNoInclusionSetting() {
		assertThat(loader.isIncludedCombination(bundles[1], "unknown"), is(Optional.empty()));
	}

	@Test(expected=ClassNotFoundException.class)
	public void loadingNullClassNameThrowsException() throws Exception {
		loader.loadClass(null);
	}

	@Test(expected=ClassNotFoundException.class)
	public void loadingEmptyClassNameThrowsException() throws Exception {
		loader.loadClass("");
	}

	@Test(expected=ClassNotFoundException.class)
	public void loadingUnknownClassNameThrowsExceptionWithoutTryingAnyBundle() throws Exception {
		try {
			loader.loadClass(BAD_CLASS_NAME);
		} catch (Exception e) {
			verify(bundles[0], never()).loadClass(BAD_CLASS_NAME);
			verify(bundles[1], never()).loadClass(BAD_CLASS_NAME);
			verify(bundles[2], never()).loadClass(BAD_CLASS_NAME);
			throw e;
		}
	}

	@Test
	public void loadingKnownClassNameReturnsTheClassOnlyUsingMatchingBundle() throws Exception {
		assertThat(loader.loadClass(GOOD_CLASS_NAME), equalTo(DummyClass.class));
		verify(bundles[0], never()).loadClass(GOOD_CLASS_NAME);
		verify(bundles[1], never()).loadClass(GOOD_CLASS_NAME);
		verify(logger).debug(contains(MAIN_CLASSLOADER_SUCCESS_MSG), eq(GOOD_CLASS_NAME), eq(bundles[2]));
	}

	@Test(expected=ClassNotFoundException.class)
	public void jarLoadingIsNotTriedForUnknownPackagesIfSysPathIsSetButDoesNotContainJars() throws Exception {
		when(sysPath.toString()).thenReturn(String.format("[%s]", FILLER));
		when(sysPath.getArray()).thenReturn(new PyObject[]{new PyString(FILLER)});
		loader.setSysPath(sysPath);
		try {
			loader.loadClass(BAD_CLASS_NAME);
		} catch (Exception e) {
			verify(sysPath, never()).getArray();
			throw e;
		}
	}

	@Test
	public void jarLoadingSucceedsForUnknownPackagesWhenClassInSpecifiedJar() throws Exception {
		when(sysPath.toString()).thenReturn(STD_SYS_PATH);
		when(sysPath.getArray()).thenReturn(new PyObject[]{new PyString(FILLER), new PyString(TEST_JAR_PATH)});
		loader.setSysPath(sysPath);
		Class<?> theClass = loader.loadClass(JAR_CLASS_NAME_ONE);
		Object theInstance = theClass.newInstance();
		Method doSomethingUseless = theClass.getMethod(METHOD_NAME);
		verify(sysPath).getArray();
		verify(logger, never()).debug(contains(MAIN_CLASSLOADER_SUCCESS_MSG), any(String.class));
		verify(logger).debug(contains(URL_CLASSLOADER_SUCCESS_MSG), eq(JAR_CLASS_NAME_ONE), contains(JAR_LOCATION));
		assertThat(theClass.getName(), is(JAR_CLASS_NAME_ONE));
		assertThat(doSomethingUseless.invoke(theInstance), is(NIGEL_RESPONSE));
	}

	@Test
	public void jarLoadingSucceedsInSpiteOfInvalidJarPathBeforeValidOneWithSpaces() throws Throwable {
		String baseBadUrl = "[646467.gdgtdlo]// /te ££$ %&&.jar";
		String badUrl = " " + baseBadUrl + " ";
		when(sysPath.toString()).thenReturn(String.format("[%s,%s,%s ]", FILLER, badUrl, TEST_JAR_PATH));
		when(sysPath.getArray()).thenReturn(new PyObject[]{new PyString(FILLER), new PyString(badUrl), new PyString(TEST_JAR_PATH)});
		loader.setSysPath(sysPath);
		Class<?> theClass = loader.loadClass(JAR_CLASS_NAME_ONE);
		Object theInstance = theClass.newInstance();
		Method doSomethingUseless = theClass.getMethod(METHOD_NAME);
		verify(sysPath).getArray();
		verify(logger, never()).debug(contains(MAIN_CLASSLOADER_SUCCESS_MSG), any(String.class));
		verify(logger).warn(contains("Unable to resolve jar file path URL for"), eq(baseBadUrl), any(MalformedURLException.class));
		verify(logger).debug(contains(URL_CLASSLOADER_SUCCESS_MSG), eq(JAR_CLASS_NAME_ONE), contains(JAR_LOCATION));
		assertThat(theClass.getName(), is(JAR_CLASS_NAME_ONE));
		assertThat(doSomethingUseless.invoke(theInstance), is(NIGEL_RESPONSE));
	}

	@Test(expected=ClassNotFoundException.class)
	public void jarLoadingFailsForUnknownPackagesWhenClassNotInSpecifiedJar() throws Exception {
		when(sysPath.toString()).thenReturn(STD_SYS_PATH);
		when(sysPath.getArray()).thenReturn(new PyObject[]{new PyString(FILLER), new PyString(TEST_JAR_PATH)});
		loader.setSysPath(sysPath);
		try {
			loader.loadClass(BAD_CLASS_NAME);
		} catch (Exception e) {
			verify(sysPath).getArray();
			throw e;
		}
	}

	@Test
	public void jarLoadingSucceedsForUnknownPackagesWhenMultipleDependantClassesInSpecifiedJarReferenced() throws Exception {
		when(sysPath.toString()).thenReturn(STD_SYS_PATH);
		when(sysPath.getArray()).thenReturn(new PyObject[]{new PyString(FILLER), new PyString(TEST_JAR_PATH)});
		loader.setSysPath(sysPath);
		Class<?> theClass = loader.loadClass(JAR_CLASS_NAME_THREE);
		Object theInstance = theClass.newInstance();
		Method getCompetitors = theClass.getMethod(METHOD_NAME_RACE);
		verify(sysPath).getArray();
		verify(logger, never()).debug(contains(MAIN_CLASSLOADER_SUCCESS_MSG), any(String.class));
		verify(logger).debug(contains(URL_CLASSLOADER_SUCCESS_MSG), eq(JAR_CLASS_NAME_THREE), contains(JAR_LOCATION));
		assertThat(theClass.getName(), is(JAR_CLASS_NAME_THREE));
		Object[] competitors = (Object[])getCompetitors.invoke(theInstance);
		assertThat(competitors[0].getClass().getName(), is(JAR_CLASS_NAME_ONE));
		Method doSomethingUseless = competitors[0].getClass().getMethod(METHOD_NAME);
		assertThat(doSomethingUseless.invoke(competitors[0]), is(NIGEL_RESPONSE));
		assertThat(competitors[1].getClass().getName(), is(JAR_CLASS_NAME_TWO));
		doSomethingUseless = competitors[1].getClass().getMethod(METHOD_NAME);
		assertThat(doSomethingUseless.invoke(competitors[1]), is(GERVAIS_RESPONSE));
	}
}
