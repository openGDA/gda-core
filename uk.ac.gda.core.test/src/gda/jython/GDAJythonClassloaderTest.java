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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.osgi.framework.Constants.EXPORT_PACKAGE;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;

import com.google.common.collect.ImmutableMap;


public class GDAJythonClassloaderTest {

	private static final String PKG_1 = "org.python.monty";
	private static final String PKG_2 = "org.python.monty.gumby";
	private static final String PKG_3 = "org.python.monty.nudge.nudge";
	private static final String PKG_4 = "org.spam.spam.eggs.spam";
	private static final String FOLDER_1 = "/tmp";
	private static final String BAD_CLASS_NAME = "NotActuallyAClass";
	private static final String GOOD_CLASS_NAME = "org.python.monty.nudge.nudge.SayNoMore";
	private Bundle[] bundles;
	private final Set<String> scriptFolders = new HashSet<>(Arrays.asList(FOLDER_1));
	private final Set<String> included = new HashSet<>(Arrays.asList("bundle2:" + PKG_4));
	private Dictionary<String, String> headers1;
	private Dictionary<String, String> headers2;
	private Dictionary<String, String> headers3;

	private class DummyClass {
	}

	@SuppressWarnings("unchecked")
	@Before
	public void setup() throws Exception {
		Bundle bundle1 = mock(Bundle.class);
		Bundle bundle2 = mock(Bundle.class);
		Bundle bundle3 = mock(Bundle.class);
		bundles = new Bundle[] {bundle1, bundle2, bundle3};
		headers1 = new Hashtable<String, String>(ImmutableMap.of(EXPORT_PACKAGE, PKG_1 + "," + PKG_2));
		headers2 = new Hashtable<String, String>(ImmutableMap.of(EXPORT_PACKAGE, PKG_4 + "," + PKG_2));
		headers3 = new Hashtable<String, String>(ImmutableMap.of(EXPORT_PACKAGE, PKG_3));


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
	}

	@Test
	public void initializationSucceeds() {
		assertThat(GDAJythonClassLoader.useGDAClassLoader(), equalTo(true));
	}

	@Test
	public void packagesAreSetAsInitialized() {
		GDAJythonClassLoader loader = new GDAJythonClassLoader();
		assertThat(loader.getJythonPackages(), hasItems(PKG_1, PKG_2, PKG_3, PKG_4));
	}

	@Test
	public void pathsAreSetAsInitialized() {
		GDAJythonClassLoader loader = new GDAJythonClassLoader();
		assertThat(loader.getStandardFolders(), hasItems(FOLDER_1));
	}

	@Test
	public void requiredPackageInclusionsAreSetAsInitialized() {
		GDAJythonClassLoader loader = new GDAJythonClassLoader();
		assertThat(loader.isIncludedCombination(bundles[1], PKG_4), not(Optional.empty()));
		assertThat(loader.isIncludedCombination(bundles[1], PKG_4).get(), is(true));
	}

	@Test
	public void requiredPackageExclusionsAreSetAsInitialized() {
		GDAJythonClassLoader loader = new GDAJythonClassLoader();
		assertThat(loader.isIncludedCombination(bundles[1], PKG_4), not(Optional.empty()));
		assertThat(loader.isIncludedCombination(bundles[0], PKG_1).get(), is(false));
		assertThat(loader.isIncludedCombination(bundles[0], PKG_2).get(), is(false));
		assertThat(loader.isIncludedCombination(bundles[1], PKG_2).get(), is(false));
		assertThat(loader.isIncludedCombination(bundles[2], PKG_3).get(), is(false));
	}

	@Test
	public void unknownPackagesHaveNoInclusionSetting() {
		GDAJythonClassLoader loader = new GDAJythonClassLoader();
		assertThat(loader.isIncludedCombination(bundles[1], "unknown"), is(Optional.empty()));
	}

	@Test(expected=ClassNotFoundException.class)
	public void loadingNullClassNameThrowsException() throws Exception {
		GDAJythonClassLoader loader = new GDAJythonClassLoader();
		loader.loadClass(null);
	}

	@Test(expected=ClassNotFoundException.class)
	public void loadingEmptyClassNameThrowsException() throws Exception {
		GDAJythonClassLoader loader = new GDAJythonClassLoader();
		loader.loadClass("");
	}

	@Test(expected=ClassNotFoundException.class)
	public void loadingUnknownClassNameThrowsExceptionWithoutTryingAnyBundle() throws Exception {
		GDAJythonClassLoader loader = new GDAJythonClassLoader();
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
		GDAJythonClassLoader loader = new GDAJythonClassLoader();
		assertThat(loader.loadClass(GOOD_CLASS_NAME), equalTo(DummyClass.class));
		verify(bundles[0], never()).loadClass(GOOD_CLASS_NAME);
		verify(bundles[1], never()).loadClass(GOOD_CLASS_NAME);
	}
}
