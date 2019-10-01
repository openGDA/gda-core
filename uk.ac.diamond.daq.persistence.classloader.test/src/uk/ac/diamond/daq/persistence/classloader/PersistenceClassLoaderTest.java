/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.persistence.classloader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;

import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.persistence.testdata.PersistenceTestClass;

public class PersistenceClassLoaderTest {

	private PersistenceClassLoader classLoader;

	@Before
	public void setUp() {
		classLoader = PersistenceClassLoader.getInstance();
	}

	@Test
	public void testInitialisation() {
		assertTrue(classLoader.isInitialised());
	}

	@Test
	public void testLoadClass() throws Exception {
		final String className = "uk.ac.diamond.daq.persistence.testdata.PersistenceTestClass";
		final String objectName = "objName";

		final Class<?> clazz = classLoader.forName(className);
		assertNotNull(clazz);
		final Class<?> clazz2 = classLoader.forName(className);
		assertNotNull(clazz2);

		// Check that we can use the class
		final Constructor<?> constructor = clazz.getConstructor(String.class);
		final PersistenceTestClass obj = (PersistenceTestClass) constructor.newInstance(objectName);
		assertEquals(objectName, obj.getName());
	}

}
