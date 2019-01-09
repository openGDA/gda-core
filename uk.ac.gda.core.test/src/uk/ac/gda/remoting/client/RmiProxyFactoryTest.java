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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.ac.gda.remoting.server.RmiAutomatedExporter.AUTO_EXPORT_RMI_PREFIX;
import static uk.ac.gda.remoting.server.RmiAutomatedExporter.RMI_PORT_PROPERTY;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

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
		// Make object under test
		rmiProxyFactory = new RmiProxyFactory();

		// Setup an exporter
		exporter = new RmiServiceExporter();
		exporter.setRegistry(registry);
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
	}

	@Test
	public void testGettingScannable() throws Exception {
		Scannable mockScannable = mock(Scannable.class);
		when(mockScannable.getName()).thenReturn("mockScannable");
		exportObject(mockScannable, "mockScannable", Scannable.class);

		// Auto import
		rmiProxyFactory.afterPropertiesSet();

		final Scannable foundMockScannable = rmiProxyFactory.getFindable("mockScannable");
		assertThat(foundMockScannable, is(notNullValue()));
		assertThat(foundMockScannable.getName(), is(equalTo("mockScannable")));
	}

	@Test
	public void testGettingFindableNames() throws Exception {
		Scannable mockScannable = mock(Scannable.class);
		when(mockScannable.getName()).thenReturn("mockScannable");
		exportObject(mockScannable, "mockScannable", Scannable.class);

		// Auto import
		rmiProxyFactory.afterPropertiesSet();

		assertThat(rmiProxyFactory.getFindableNames(), contains("mockScannable"));
	}

	@Test
	public void testGettingAllFindables() throws Exception {
		Scannable mockScannable = mock(Scannable.class);
		when(mockScannable.getName()).thenReturn("mockScannable");
		exportObject(mockScannable, "mockScannable", Scannable.class);

		// Auto import
		rmiProxyFactory.afterPropertiesSet();

		assertThat(rmiProxyFactory.getFindables(), hasSize(1));
		assertThat(rmiProxyFactory.getFindables().iterator().next().getName(), is("mockScannable"));
	}

	@Test
	public void testGettingScannableViaFinder() throws Exception {
		Scannable mockScannable = mock(Scannable.class);
		when(mockScannable.getName()).thenReturn("mockScannable");
		exportObject(mockScannable, "mockScannable", Scannable.class);

		// Auto import
		rmiProxyFactory.afterPropertiesSet();

		// Also check through the finder
		final Scannable foundMockScannable = Finder.getInstance().find("mockScannable");
		assertThat(foundMockScannable, is(notNullValue()));
		assertThat(foundMockScannable.getName(), is(equalTo("mockScannable")));
	}

	@Test
	public void testCallsAreMadeOnTheRemoteObject() throws Exception {
		Scannable mockScannable = mock(Scannable.class);
		when(mockScannable.isBusy()).thenReturn(false);
		when(mockScannable.getPosition()).thenReturn(33.34);
		when(mockScannable.getName()).thenReturn("mockScannable");

		exportObject(mockScannable, "mockScannable", Scannable.class);

		// Auto import
		rmiProxyFactory.afterPropertiesSet();
		Scannable remoteScannable = rmiProxyFactory.getFindable("mockScannable");

		// Check methods calls work
		assertThat(remoteScannable.getName(), is("mockScannable"));
		assertThat((Double) remoteScannable.getPosition(), is(closeTo(33.34, 0.01)));
		assertThat(remoteScannable.isBusy(), is(equalTo(false)));

		// Check the right number of calls were made
		verify(mockScannable, times(1)).getPosition();
		verify(mockScannable, times(1)).isBusy();
	}

	@Test
	public void testIObservableCallsAreNotMadeOnTheRemoteObject() throws Exception {
		Scannable mockScannable = mock(Scannable.class);
		when(mockScannable.getName()).thenReturn("mockScannable");

		exportObject(mockScannable, "mockScannable", Scannable.class);

		// Auto import
		rmiProxyFactory.afterPropertiesSet();
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
	}

	@Test
	public void testContainsExportableObjectsReturnsFalse() throws Exception {
		assertThat(rmiProxyFactory.containsExportableObjects(), is(equalTo(false)));
	}

	@Test
	public void testIsLocalReturnsFalse() throws Exception {
		assertThat(rmiProxyFactory.isLocal(), is(equalTo(false)));
	}

	@Test(expected=IllegalStateException.class)
	public void testGetFindableThrowsWhenNotConfigured() throws Exception {
		// Should throw
		rmiProxyFactory.getFindable("anything");
	}

	@Test(expected=IllegalStateException.class)
	public void testGetFindableNamesThrowsWhenNotConfigured() throws Exception {
		// Should throw
		rmiProxyFactory.getFindableNames();
	}

	@Test(expected=IllegalStateException.class)
	public void testGetFindablesThrowsWhenNotConfigured() throws Exception {
		// Should throw
		rmiProxyFactory.getFindables();
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testAddFindableThrows() throws Exception {
		// Should throw you can't add objects
		rmiProxyFactory.addFindable(mock(Findable.class));
	}

}
