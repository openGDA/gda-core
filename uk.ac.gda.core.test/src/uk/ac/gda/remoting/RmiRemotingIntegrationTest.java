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

package uk.ac.gda.remoting;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static uk.ac.gda.remoting.server.RmiAutomatedExporter.RMI_PORT_PROPERTY;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.util.SocketUtils;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableBase;
import gda.factory.Factory;
import gda.factory.Findable;
import gda.factory.Finder;
import uk.ac.gda.api.remoting.ServiceInterface;
import uk.ac.gda.remoting.client.RmiProxyFactory;
import uk.ac.gda.remoting.server.RmiAutomatedExporter;

/**
 * This is a test of both {@link RmiAutomatedExporter} and {@link RmiProxyFactory} working together.
 *
 * @since GDA 9.8
 * @author James Mudd
 */
public class RmiRemotingIntegrationTest {

	private static int portForTesting;

	@Mock
	private Factory mockFactory;

	// Under test
	private RmiAutomatedExporter rmiAutoExporter;

	private RmiProxyFactory rmiProxyFactory;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		LocalProperties.forceActiveMQEmbeddedBroker(); // Use in JVM broker
		// Need to find a free port as this test might be running simultaneously on the same machine
		portForTesting = SocketUtils.findAvailableTcpPort(1099, 10000);
		// Set properties
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
	}

	@SuppressWarnings("unused") // As the RmiProxyFactory adds itself to the Finder
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(mockFactory.isLocal()).thenReturn(true);

		// Make objects under test
		rmiAutoExporter = new RmiAutomatedExporter();

		// Making this add itself to the Finder
		rmiProxyFactory = new RmiProxyFactory();

		// Add the mockFactory second this is dubious and relies on the actual set implementation in the Finder being
		// ordered
		Finder.getInstance().addFactory(mockFactory);
	}

	@After
	public void tearDown() throws Exception {
		rmiAutoExporter.shutdown();
		// Clean out the Finder
		Finder.getInstance().removeAllFactories();
	}

	@Test
	public void testObjectCanBeExportedAndImported() throws Exception {
		Findable testObj = new TestScannable("testObj");
		when(mockFactory.getFindable("testObj")).thenReturn(testObj);

		final Scannable foundScannable = Finder.getInstance().find("testObj");
		assertThat(foundScannable, is(notNullValue()));
		assertThat(foundScannable, isA(Scannable.class)); // Check type is that of @ServiceInterface
	}

	@Test
	public void testObjectCanBeFoundByType() throws Exception {
		Scannable testObj = new TestScannable("testObj");
		Map<String, Scannable> objects = new HashMap<>();
		objects.put("testObj", testObj);
		when(mockFactory.getFindablesOfType(Scannable.class)).thenReturn(objects);
		when(mockFactory.getFindable("testObj")).thenReturn(testObj);

		// Reorder the finder so the rmi factory is last
		Finder.getInstance().removeAllFactories();
		Finder.getInstance().addFactory(mockFactory);
		Finder.getInstance().addFactory(rmiProxyFactory);

		// New search order for Finder (local first) meant that we will not get the scannables
		// from the remote factory first, so use the factory directly
		final Map<String, Scannable> foundScannables = rmiProxyFactory.getFindablesOfType(Scannable.class);
		assertThat(foundScannables.size(), is(equalTo(1)));
		assertThat(foundScannables.get("testObj"), is(notNullValue()));
		// Check we have got the RMI proxy
		assertThat(Proxy.isProxyClass(foundScannables.get("testObj").getClass()), is(true));
	}

	@Test
	public void testEventsCanBeReceivedByClientProxies() throws Exception {
		TestScannable testScannable = new TestScannable("testScannable");
		when(mockFactory.getFindable("testScannable")).thenReturn(testScannable);

		// New search order for Finder (local first) meant that we will not get the scannable
		// from the remote factory first, so use the factory directly
		final Scannable foundScannable = rmiProxyFactory.getFindable("testScannable");
		// Check we have got the RMI proxy
		assertThat(Proxy.isProxyClass(foundScannable.getClass()), is(true));

		// Latch to ensure the observer is notified before the test ends.
		final CountDownLatch latch = new CountDownLatch(1);

		foundScannable.addIObserver((source, arg) -> {
			// The source you have here is actually the proxy so the "real" source is lost.
			// Not sure if this is a problem
			assertThat(arg, is(equalTo("event")));
			latch.countDown();
		});

		// Send an event from the "server" side
		testScannable.sendEvent("source", "event");

		// Wait for the observer to be notified
		assertThat("No event was received in 5 seconds", latch.await(5, SECONDS)); // If not done in 5 sec the test will
																					// fail
	}

	@ServiceInterface(Scannable.class)
	private class TestScannable extends ScannableBase {
		private String name;

		public TestScannable(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void setName(String name) {
			this.name = name;
		}

		@Override
		public boolean isBusy() throws DeviceException {
			return false;
		}

		public void sendEvent(Object source, Object event) {
			notifyIObservers(source, event);
		}
	}

}
