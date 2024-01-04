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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import gda.device.Scannable;

class FinderTest {

	@AfterEach
	public void cleanUpFinder() {
		Finder.removeAllFactories();
	}

	@Test
	void findSingleton() {
		prepareFactoryForSingletonTests();
		SingletonService found = Finder.findSingleton(SingletonService.class);
		assertThat(found, is(notNullValue()));
		assertThat(found.getName(), is(equalTo("singleton")));
	}

	@Test
	void findSingletonMultipleInstancesThrows() {
		prepareFactoryForSingletonTests();
		assertThrows(IllegalArgumentException.class, () -> Finder.findSingleton(SomeOtherFindable.class));
	}

	@Test
	void findSingletonNoInstancesThrows() {
		// not adding anything to the Finder factories
		assertThrows(IllegalArgumentException.class, () -> Finder.findSingleton(SomeOtherFindable.class));
	}

	@Test
	void findSingletonMultipleAliases() {
		prepareFactoryForSingletonTests();

		SingletonService found = Finder.findSingleton(SingletonService.class);
		assertThat(found, is(notNullValue()));
		assertThat(found.getName(), is(equalTo("singleton")));

		SingletonService localFound = Finder.findLocalSingleton(SingletonService.class);
		assertThat(localFound, is(notNullValue()));
		assertThat(localFound, is(sameInstance(found)));

		Optional<SingletonService> optFound = Finder.findOptionalSingleton(SingletonService.class);
		assertThat(optFound.isPresent(), is(true));
		assertThat(optFound.orElseThrow(), is(sameInstance(found)));

		Optional<SingletonService> optLocalFound = Finder.findOptionalSingleton(SingletonService.class);
		assertThat(optLocalFound.isPresent(), is(true));
		assertThat(optLocalFound.orElseThrow(), is(sameInstance(found)));
	}

	@Test
	void findOptionalOfTypeReturnsOptionalWithCorrectFindableWhenNameAndTypeMatch() throws FactoryException {
		prepareFactoryForFindAndFindOptionalTests();

		Optional<Findable> findable = Finder.findOptionalOfType("findable1", Findable.class);

		assertThat(findable.isPresent(), is(true));
		assertThat(findable.get().getName(), is(equalTo("findable1")));
	}

	@Test
	void findOptionalOfTypeReturnsEmptyOptionalWhenNameMatchesButTypeDoesNot() throws FactoryException {
		prepareFactoryForFindAndFindOptionalTests();

		Optional<Scannable> findable = Finder.findOptionalOfType("findable1", Scannable.class);

		assertThat(findable.isPresent(), is(false));
	}

	@Test
	void findReturnsCorrectFindable() throws FactoryException {
		prepareFactoryForFindAndFindOptionalTests();

		Findable findable = Finder.find("findable1");

		assertThat(findable, is(notNullValue()));
		assertThat(findable.getName(), is(equalTo("findable1")));
	}

	@Test
	void findReturnsNullWhenNotFound() throws FactoryException {
		prepareFactoryForFindAndFindOptionalTests();

		Findable findable = Finder.find("findable3");
		assertThat(findable, is(nullValue()));
	}

	@Test
	void finderFindsLocalFindableFirst() throws FactoryException {
		prepareLocalAndRemoteFactoryWithSameNameFindables();

		Findable findable = Finder.find("findable1");

		assertThat(findable, is(notNullValue()));
		assertThat(findable.getClass(), is(equalTo(SomeOtherFindable.class)));
	}

	@Test
	void listAllInterfacesCorrectlyListsAllInterfaces() {
		prepareFactoryForListAllInterfacesTest();

		List<String> interfaces = Finder.listAllInterfaces();
		assertThat(interfaces, containsInAnyOrder(
				"FinderTest$Interface1", "FinderTest$Interface2",
				"FinderTest$Interface3", "FinderTest$Interface4",
				"Findable"));
	}

	@Test
	void getFindablesOfTypeReturnsCorrectFindables() throws FactoryException {
		prepareLocalAndRemoteFactory();

		Map<String, SomeFindable> findables = Finder.getFindablesOfType(SomeFindable.class);

		assertThat(findables.get("findable1"), is(notNullValue()));
		assertThat(findables.get("findable1").getName(), is(equalTo("findable1")));
		assertThat(findables.get("findable2"), is(notNullValue()));
		assertThat(findables.get("findable2").getName(), is(equalTo("findable2")));
		assertThat(findables.size(), is(2));
	}

	@Test
	void getLocalFindablesOfTypeReturnsLocalFindablesOnly() throws FactoryException {
		prepareLocalAndRemoteFactory();

		Map<String, SomeFindable> findables = Finder.getLocalFindablesOfType(SomeFindable.class);

		assertThat(findables.get("findable1"), is(notNullValue()));
		assertThat(findables.get("findable1").getName(), is(equalTo("findable1")));
		assertThat(findables.get("findable2"), is(nullValue()));
		assertThat(findables.size(), is(1));
	}

	@Test
	void listFindablesOfTypeReturnsCorrectFindables() throws FactoryException {
		prepareLocalAndRemoteFactory();

		List<SomeFindable> findables = Finder.listFindablesOfType(SomeFindable.class);

		assertThat(findables.size(), is(2));
		List<String> names = findables.stream().map(Findable::getName).toList();
		assertThat(names, containsInAnyOrder("findable1", "findable2"));
	}

	@Test
	void listLocalFindablesOfTypeReturnsCorrectLocalFindables() throws FactoryException {
		prepareLocalAndRemoteFactory();

		List<SomeFindable> findables = Finder.listLocalFindablesOfType(SomeFindable.class);

		assertThat(findables.size(), is(1));
		assertThat(findables.get(0).getName(), is("findable1"));
	}

	private void prepareFactoryForSingletonTests() {
		SingletonService singleton = new SingletonService("singleton");
		Findable notSingleton1 = new SomeOtherFindable("notASingleton1");
		Findable notSingleton2 = new SomeOtherFindable("notASingleton2");

		Map<String, SingletonService> singletonServices = Map.of("singleton", singleton,
				"singleton2", singleton);

		Factory testFactory = mock(Factory.class);
		when(testFactory.isLocal()).thenReturn(true);
		when(testFactory.getFindables())
			.thenReturn(Arrays.asList(singleton, notSingleton1, notSingleton2));
		when(testFactory.getFindablesOfType(SingletonService.class)).thenReturn(singletonServices);

		Finder.addFactory(testFactory);
	}

	private void prepareFactoryForFindAndFindOptionalTests() throws FactoryException {
		Findable findable1 = new SomeOtherFindable("findable1");
		Findable findable2 = new SomeOtherFindable("findable2");

		Map<String, Findable> findables = Map.of(
				"findable1", findable1,
				"findable2", findable2);

		Factory testFactory = mock(Factory.class);
		when(testFactory.getFindables())
			.thenReturn(Arrays.asList(findable1, findable2));
		when(testFactory.getFindablesOfType(Findable.class))
			.thenReturn(findables);
		when(testFactory.getFindable("findable1"))
			.thenReturn(findable1);

		Finder.addFactory(testFactory);
	}

	private void prepareFactoryForListAllInterfacesTest() {
		Findable findable1 = new SomeFindable("findable1");
		Findable findable2 = new SomeOtherFindable("findable2");

		Map<String, Findable> findables = Map.of(
				"findable1", findable1,
				"findable2", findable2);

		Factory testFactory = mock(Factory.class);
		when(testFactory.getFindables())
			.thenReturn(Arrays.asList(findable1, findable2));
		when(testFactory.getFindablesOfType(Findable.class))
			.thenReturn(findables);

		Finder.addFactory(testFactory);
	}

	private void prepareLocalAndRemoteFactory() throws FactoryException {
		SomeFindable findable1 = new SomeFindable("findable1");
		SomeFindable findable2 = new SomeFindable("findable2");

		Map<String, SomeFindable> localFindables = Map.of("findable1", findable1);
		Map<String, SomeFindable> remoteFindables = Map.of("findable2", findable2);

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

		Finder.addFactory(localFactory);
		Finder.addFactory(remoteFactory);
	}

	private void prepareLocalAndRemoteFactoryWithSameNameFindables() throws FactoryException {
		SomeFindable localFindable = new SomeFindable("findable1");
		SomeOtherFindable remoteFindable = new SomeOtherFindable("findable1");

		Map<String, SomeFindable> localFindables = Map.of("findable1", localFindable);
		Map<String, SomeOtherFindable> remoteFindables = Map.of("findable2", remoteFindable);

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

		Finder.addFactory(localFactory);
		Finder.addFactory(remoteFactory);
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
