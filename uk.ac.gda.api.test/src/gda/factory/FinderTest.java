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
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Test;

public class FinderTest {

	@After
	public void cleanUpFinder() {
		Finder.getInstance().removeAllFactories();
	}

	@Test
	public void findSingleton() {
		prepareFactoryForSingletonTests();
		SingletonService found = Finder.getInstance().findSingleton(SingletonService.class);
		assertNotNull(found);
		assertEquals("singleton", found.getName());
	}

	@Test(expected=IllegalArgumentException.class)
	public void findSingletonMultipleInstancesThrows() {
		prepareFactoryForSingletonTests();
		Finder.getInstance().findSingleton(SomeOtherFindable.class);
	}

	@Test(expected=IllegalArgumentException.class)
	public void findSingletonNoInstancesThrows() {
		// not adding anything to the Finder factories
		Finder.getInstance().findSingleton(SingletonService.class);
	}

	private void prepareFactoryForSingletonTests() {
		SingletonService singleton = new SingletonService("singleton");
		Findable notSingleton1 = new SomeOtherFindable("notASingleton1");
		Findable notSingleton2 = new SomeOtherFindable("notASingleton2");

		Map<String, SingletonService> singletonServices = new HashMap<>();
		singletonServices.put("singleton", singleton);

		Factory testFactory = mock(Factory.class);
		when(testFactory.getFindables())
			.thenReturn(Arrays.asList(singleton, notSingleton1, notSingleton2));
		when(testFactory.getFindablesOfType(SingletonService.class)).thenReturn(singletonServices);

		Finder.getInstance().addFactory(testFactory);
	}

	private class SingletonService extends FindableBase {
		public SingletonService(String name) {
			setName(name);
		}
	}

	private class SomeOtherFindable extends FindableBase {
		public SomeOtherFindable(String name) {
			setName(name);
		}
	}
}
