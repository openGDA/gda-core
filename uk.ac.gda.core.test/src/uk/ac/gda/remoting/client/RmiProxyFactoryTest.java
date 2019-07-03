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

package uk.ac.gda.remoting.client;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.ac.gda.remoting.server.RmiAutomatedExporter.AUTO_EXPORT_RMI_PREFIX;
import static uk.ac.gda.remoting.server.RmiAutomatedExporter.RMI_PORT_PROPERTY;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.remoting.rmi.RmiServiceExporter;
import org.springframework.util.SocketUtils;

import gda.configuration.properties.LocalProperties;
import gda.device.Scannable;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.observable.IObserver;
import uk.ac.gda.remoting.server.RmiAutomatedExporter;
import uk.ac.gda.remoting.server.RmiObjectInfo;
import uk.ac.gda.remoting.server.RmiRemoteObjectProvider;

/**
 * This test automated RMI importing by actually importing services locally then checking they can be found and use.
 * <p>
 * Note: This is not a pure unit test it uses sockets for this reason it tries to be nice by finding an available port.
 *
 * @author James Mudd
 */
public class RmiProxyFactoryTest {

	private static int portForTesting;

	private static Registry registry;

	// Under test
	private RmiProxyFactory rmiProxyFactory;

	private RmiServiceExporter exporter;

	private RmiRemoteObjectProvider mockRemoteObjectProvider;

	private Set<String> exportedNames;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		LocalProperties.forceActiveMQEmbeddedBroker(); // Use in JVM broker
		// Need to find a free port as this test might be running simultaneously on the same machine
		portForTesting = SocketUtils.findAvailableTcpPort(1099, 10000);
		// Create a registry
		registry = LocateRegistry.createRegistry(portForTesting);
		// Set the property this is used by the RmiAutomatedExporter
		LocalProperties.set(RMI_PORT_PROPERTY, Integer.toString(portForTesting));
		LocalProperties.set("gda.server.host", "localhost");
		// Ensure no previous tests have left factories attached to the Finder
		Finder.getInstance().removeAllFactories();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		// Cleanup properties set
		LocalProperties.unsetActiveMQBrokerURI();
		LocalProperties.clearProperty(RMI_PORT_PROPERTY);
		LocalProperties.clearProperty("gda.server.host");

		// Close the RMI registry
		UnicastRemoteObject.unexportObject(registry, true);
	}

	@Before
	public void setUp() throws Exception {
		// Setup an exporter
		exporter = new RmiServiceExporter();
		exporter.setRegistry(registry);

		// Start with new empty set of exported names
		exportedNames = new HashSet<>();

		// Setup mock remote object provider
		mockRemoteObjectProvider = mock(RmiRemoteObjectProvider.class);
		exportObject(mockRemoteObjectProvider, RmiAutomatedExporter.REMOTE_OBJECT_PROVIDER, RmiRemoteObjectProvider.class);

		// Make object under test
		rmiProxyFactory = new RmiProxyFactory();
		rmiProxyFactory.configure();
	}

	@After
	public void tearDown() throws Exception {
		// Clean out all exported objects
		for (String name : registry.list()) {
			registry.unbind(name);
		}

		// Clean out the Finder
		Finder.getInstance().removeAllFactories();
	}

	private void exportObject(Object obj, String name, Class<?> serviceInterface) throws RemoteException {
		exporter.setService(obj);
		exporter.setServiceName(AUTO_EXPORT_RMI_PREFIX + name);
		exporter.setServiceInterface(serviceInterface);
		exporter.afterPropertiesSet(); // Do the export

		// Build RmiObjectInfo
		RmiObjectInfo info = new RmiObjectInfo(name, AUTO_EXPORT_RMI_PREFIX + name, serviceInterface.getCanonicalName(), true);
		when(mockRemoteObjectProvider.getRemoteObject(name)).thenReturn(info);

		if(Findable.class.isAssignableFrom(serviceInterface)) {
			exportedNames.add(name);
		}
		when(mockRemoteObjectProvider.getRemoteObjectNamesImplementingType(anyString())).thenReturn(exportedNames);
	}

	@Test
	public void testGettingScannable() throws Exception {
		Scannable mockScannable = mock(Scannable.class);
		when(mockScannable.getName()).thenReturn("mockScannable");
		exportObject(mockScannable, "mockScannable", Scannable.class);

		final Scannable foundMockScannable = rmiProxyFactory.getFindable("mockScannable");
		assertThat(foundMockScannable, is(notNullValue()));
		assertThat(foundMockScannable.getName(), is(equalTo("mockScannable")));

		verify(mockRemoteObjectProvider).getRemoteObject("mockScannable");
		verifyNoMoreInteractions(mockRemoteObjectProvider);
	}

	@Test
	public void testGettingFindableNames() throws Exception {
		Scannable mockScannable = mock(Scannable.class);
		when(mockScannable.getName()).thenReturn("mockScannable");
		exportObject(mockScannable, "mockScannable", Scannable.class);

		assertThat(rmiProxyFactory.getFindableNames(), contains("mockScannable"));

		verify(mockRemoteObjectProvider).getRemoteObjectNamesImplementingType(Findable.class.getCanonicalName());
		verifyNoMoreInteractions(mockRemoteObjectProvider);
	}

	@Test
	public void testGettingAllFindables() throws Exception {
		Scannable mockScannable1 = mock(Scannable.class);
		when(mockScannable1.getName()).thenReturn("mockScannable1");
		exportObject(mockScannable1, "mockScannable1", Scannable.class);

		Scannable mockScannable2 = mock(Scannable.class);
		when(mockScannable2.getName()).thenReturn("mockScannable2");
		exportObject(mockScannable2, "mockScannable2", Scannable.class);

		List<Findable> findables = rmiProxyFactory.getFindables();
		assertThat(findables, hasSize(2));
		assertThat(findables.stream().map(Findable::getName).collect(toList()), containsInAnyOrder("mockScannable1", "mockScannable2"));

		// Verify mock interactions
		verify(mockRemoteObjectProvider).getRemoteObjectNamesImplementingType(Findable.class.getCanonicalName());
		verify(mockRemoteObjectProvider).getRemoteObject("mockScannable1");
		verify(mockRemoteObjectProvider).getRemoteObject("mockScannable2");
		verifyNoMoreInteractions(mockRemoteObjectProvider);
	}

	@Test
	public void testGettingScannableViaFinder() throws Exception {
		Scannable mockScannable = mock(Scannable.class);
		when(mockScannable.getName()).thenReturn("mockScannable");
		exportObject(mockScannable, "mockScannable", Scannable.class);

		// Also check through the finder
		final Scannable foundMockScannable = Finder.getInstance().find("mockScannable");
		assertThat(foundMockScannable, is(notNullValue()));
		assertThat(foundMockScannable.getName(), is(equalTo("mockScannable")));

		// Verify mock interactions
		verify(mockRemoteObjectProvider).getRemoteObject("mockScannable");
		verifyNoMoreInteractions(mockRemoteObjectProvider);
	}

	@Test
	public void testCallsAreMadeOnTheRemoteObject() throws Exception {
		Scannable mockScannable = mock(Scannable.class);
		when(mockScannable.isBusy()).thenReturn(false);
		when(mockScannable.getPosition()).thenReturn(33.34);
		when(mockScannable.getName()).thenReturn("mockScannable");

		exportObject(mockScannable, "mockScannable", Scannable.class);

		Scannable remoteScannable = rmiProxyFactory.getFindable("mockScannable");

		// Check methods calls work
		assertThat(remoteScannable.getName(), is("mockScannable"));
		assertThat((Double) remoteScannable.getPosition(), is(closeTo(33.34, 0.01)));
		assertThat(remoteScannable.isBusy(), is(equalTo(false)));

		// Check the right number of calls were made
		verify(mockScannable, times(1)).getPosition();
		verify(mockScannable, times(1)).isBusy();

		// Verify mock interactions with mockRemoteObjectProvider
		verify(mockRemoteObjectProvider).getRemoteObject("mockScannable");
		verifyNoMoreInteractions(mockRemoteObjectProvider);
	}

	@Test
	public void testIObservableCallsAreNotMadeOnTheRemoteObject() throws Exception {
		Scannable mockScannable = mock(Scannable.class);
		when(mockScannable.getName()).thenReturn("mockScannable");

		exportObject(mockScannable, "mockScannable", Scannable.class);

		Scannable remoteScannable = rmiProxyFactory.getFindable("mockScannable");

		// Make IObservable method calls which should be intercepted on the client side
		IObserver observer = mock(IObserver.class);
		remoteScannable.addIObserver(observer);
		remoteScannable.deleteIObserver(observer);
		remoteScannable.deleteIObservers();

		// Check the IObservable method calls never made it to the real object on the server side
		verify(mockScannable, never()).addIObserver(anyObject());
		verify(mockScannable, never()).deleteIObserver(anyObject());
		verify(mockScannable, never()).deleteIObservers();

		// Verify mock interactions with mockRemoteObjectProvider
		verify(mockRemoteObjectProvider).getRemoteObject("mockScannable");
		verifyNoMoreInteractions(mockRemoteObjectProvider);
	}

	@Test
	public void testIsLocalReturnsFalse() throws Exception {
		assertThat(rmiProxyFactory.isLocal(), is(equalTo(false)));

		verifyNoMoreInteractions(mockRemoteObjectProvider);
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testAddFindableThrows() throws Exception {
		// Should throw you can't add objects
		rmiProxyFactory.addFindable(mock(Findable.class));

		verifyNoMoreInteractions(mockRemoteObjectProvider);
	}

}
