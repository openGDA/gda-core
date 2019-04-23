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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

	@Test
	public void findOptionalReturnsOptionalWithCorrectFindable() throws FactoryException {
		prepareFactoryForFindAndFindOptionalTests();

		Optional<Findable> findable = Finder.getInstance().findOptional("findable1");

		assertTrue(findable.isPresent());
		assertEquals("findable1", findable.get().getName());
	}

	@Test
	public void findOptionalReturnsOptionalWithNoFindable() throws FactoryException {
		prepareFactoryForFindAndFindOptionalTests();

		Optional<Findable> findable = Finder.getInstance().findOptional("findable3");

		assertFalse(findable.isPresent());
	}

	@Test
	public void findReturnsCorrectFindable() throws FactoryException {
		prepareFactoryForFindAndFindOptionalTests();

		Findable findable = Finder.getInstance().find("findable1");

		assertTrue(findable != null);
		assertEquals("findable1", findable.getName());
	}

	@Test
	public void findReturnsNullWhenNotFound() throws FactoryException {
		prepareFactoryForFindAndFindOptionalTests();

		Findable findable = Finder.getInstance().find("findable3");

		assertTrue(findable == null);
	}

	@Test
	public void finderFindsLocalFindableFirst() throws FactoryException {
		prepareLocalAndRemoteFactoryWithSameNameFindables();

		Findable findable = Finder.getInstance().find("findable1");

		assertNotNull(findable);
		assertEquals(SomeOtherFindable.class, findable.getClass());
	}

	@Test
	public void listAllInterfacesCorrectlyListsAllInterfaces() {
		prepareFactoryForListAllInterfacesTest();

		List<String> interfaces = Finder.getInstance().listAllInterfaces();

		assertTrue(interfaces.contains("FinderTest$Interface1"));
		assertTrue(interfaces.contains("FinderTest$Interface2"));
		assertTrue(interfaces.contains("FinderTest$Interface3"));
		assertTrue(interfaces.contains("FinderTest$Interface4"));
		assertTrue(interfaces.contains("Findable"));
		assertEquals(5, interfaces.size());
	}

	@Test
	public void listAllNamesCorrectlyListsAllNames() throws FactoryException {
		prepareLocalAndRemoteFactory();

		List<String> names = Finder.getInstance().listAllNames("gda.factory.FinderTest$Interface1");

		assertTrue(names.contains("findable1"));
		assertTrue(names.contains("findable2"));
		assertEquals(2, names.size());
	}

	@Test
	public void getFindablesOfTypeReturnsCorrectFindables() throws FactoryException {
		prepareLocalAndRemoteFactory();

		Map<String, SomeFindable> findables = Finder.getInstance().getFindablesOfType(SomeFindable.class);

		assertNotNull(findables.get("findable1"));
		assertEquals("findable1", findables.get("findable1").getName());
		assertNotNull(findables.get("findable2"));
		assertEquals("findable2", findables.get("findable2").getName());
		assertEquals(2, findables.size());
	}

	@Test
	public void getLocalFindablesOfTypeReturnsLocalFindablesOnly() throws FactoryException {
		prepareLocalAndRemoteFactory();

		Map<String, SomeFindable> findables = Finder.getInstance().getLocalFindablesOfType(SomeFindable.class);

		assertNotNull(findables.get("findable1"));
		assertEquals("findable1", findables.get("findable1").getName());
		assertNull(findables.get("findable2"));
		assertEquals(1, findables.size());
	}

	@Test
	public void listFindablesOfTypeReturnsCorrectFindables() throws FactoryException {
		prepareLocalAndRemoteFactory();

		List<SomeFindable> findables = Finder.getInstance().listFindablesOfType(SomeFindable.class);

		assertEquals(2, findables.size());

		SomeFindable findableA = findables.get(0);
		SomeFindable findableB = findables.get(1);

		assertTrue((findableA.getName() == "findable1" && findableB.getName() == "findable2") ||
				findableA.getName() == "findable2" && findableB.getName() == "findable1");
	}

	@Test
	public void listLocalFindablesOfTypeReturnsCorrectLocalFindables() throws FactoryException {
		prepareLocalAndRemoteFactory();

		List<SomeFindable> findables = Finder.getInstance().listLocalFindablesOfType(SomeFindable.class);

		assertEquals(1, findables.size());
		assertEquals("findable1", findables.get(0).getName());
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

	private void prepareFactoryForFindAndFindOptionalTests() throws FactoryException {
		Findable findable1 = new SomeOtherFindable("findable1");
		Findable findable2 = new SomeOtherFindable("findable2");

		Map<String, Findable> findables = new HashMap<>();
		findables.put("findable1", findable1);
		findables.put("findable1", findable1);

		Factory testFactory = mock(Factory.class);
		when(testFactory.getFindables())
			.thenReturn(Arrays.asList(findable1, findable2));
		when(testFactory.getFindablesOfType(Findable.class))
			.thenReturn(findables);
		when(testFactory.getFindable("findable1"))
			.thenReturn(findable1);

		Finder.getInstance().addFactory(testFactory);
	}

	private void prepareFactoryForListAllInterfacesTest() {
		Findable findable1 = new SomeFindable("findable1");
		Findable findable2 = new SomeOtherFindable("findable2");

		Map<String, Findable> findables = new HashMap<>();
		findables.put("findable1", findable1);
		findables.put("findable2", findable2);

		Factory testFactory = mock(Factory.class);
		when(testFactory.getFindables())
			.thenReturn(Arrays.asList(findable1, findable2));
		when(testFactory.getFindablesOfType(Findable.class))
			.thenReturn(findables);

		Finder.getInstance().addFactory(testFactory);
	}

	private void prepareLocalAndRemoteFactory() throws FactoryException {
		SomeFindable findable1 = new SomeFindable("findable1");
		SomeFindable findable2 = new SomeFindable("findable2");

		Map<String, SomeFindable> localFindables = new HashMap<>();
		localFindables.put("findable1", findable1);

		Map<String, SomeFindable> remoteFindables = new HashMap<>();
		remoteFindables.put("findable2", findable2);

		Factory localFactory = mock(Factory.class);
		when(localFactory.getFindables())
			.thenReturn(Arrays.asList(findable1));
		when(localFactory.getFindablesOfType(SomeFindable.class))
			.thenReturn(localFindables);
		when(localFactory.isLocal())
			.thenReturn(true);
		when(localFactory.getFindable("findable1"))
			.thenReturn(findable1);

		Factory remoteFactory = mock(Factory.class);
		when(remoteFactory.getFindables())
			.thenReturn(Arrays.asList(findable2));
		when(remoteFactory.getFindablesOfType(SomeFindable.class))
			.thenReturn(remoteFindables);
		when(remoteFactory.isLocal())
			.thenReturn(false);
		when(remoteFactory.getFindable("findable2"))
			.thenReturn(findable2);

		Finder finder = Finder.getInstance();
		finder.addFactory(localFactory);
		finder.addFactory(remoteFactory);
	}

	private void prepareLocalAndRemoteFactoryWithSameNameFindables() throws FactoryException {
		SomeFindable localFindable = new SomeFindable("findable1");
		SomeOtherFindable remoteFindable = new SomeOtherFindable("findable1");

		Map<String, SomeFindable> localFindables = new HashMap<>();
		localFindables.put("findable1", localFindable);

		Map<String, SomeOtherFindable> remoteFindables = new HashMap<>();
		remoteFindables.put("findable2", remoteFindable);

		Factory localFactory = mock(Factory.class);
		when(localFactory.getFindables())
			.thenReturn(Arrays.asList(localFindable));
		when(localFactory.getFindablesOfType(SomeFindable.class))
			.thenReturn(localFindables);
		when(localFactory.isLocal())
			.thenReturn(true);
		when(localFactory.getFindable("findable1"))
			.thenReturn(localFindable);

		Factory remoteFactory = mock(Factory.class);
		when(remoteFactory.getFindables())
			.thenReturn(Arrays.asList(remoteFindable));
		when(remoteFactory.getFindablesOfType(SomeOtherFindable.class))
			.thenReturn(remoteFindables);
		when(remoteFactory.isLocal())
			.thenReturn(false);
		when(localFactory.getFindable("findable1"))
		.thenReturn(remoteFindable);

		Finder finder = Finder.getInstance();
		finder.addFactory(localFactory);
		finder.addFactory(remoteFactory);
	}

	private class SingletonService extends FindableBase {
		public SingletonService(String name) {
			setName(name);
		}
	}

	private class SomeFindable extends FindableBase implements Interface1, Interface2 {
		public SomeFindable(String name) {
			setName(name);
		}
	}

	private class SomeOtherFindable extends FindableBase implements Interface3, Interface4{
		public SomeOtherFindable(String name) {
			setName(name);
		}
	}

	private interface Interface1 {}
	private interface Interface2 {}
	private interface Interface3 {}
	private interface Interface4 {}
}
