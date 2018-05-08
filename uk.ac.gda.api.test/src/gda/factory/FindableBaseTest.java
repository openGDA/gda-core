/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package gda.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

public class FindableBaseTest {

	private Findable findable;

	@Before
	public void setUp() {
		findable = new FindableBaseForTest();
	}

	@Test
	public void testNameInitiallyNull() {
		assertNull(findable.getName());
	}

	@Test
	public void testSetName() {
		final String name = "some_name";
		findable.setName(name);
		assertEquals(name, findable.getName());
	}

	@Test
	public void testSetNameOverwritesExistingName() {
		final String name1 = "some_name_1";
		final String name2 = "some_name_2";
		findable.setName(name1);
		findable.setName(name2);
		assertEquals(name2, findable.getName());
	}

	/**
	 * Null extension for testing of abstract class {@link gda.factory.FindableBase}
	 */
	private class FindableBaseForTest extends FindableBase {
	}

}
