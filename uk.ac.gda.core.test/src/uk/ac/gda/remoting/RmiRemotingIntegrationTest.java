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

import static gda.factory.corba.util.EventService.USE_JMS_EVENTS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static uk.ac.gda.remoting.server.RmiAutomatedExporter.RMI_PORT_PROPERTY;

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
import org.springframework.context.ApplicationContext;
import org.springframework.util.SocketUtils;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableBase;
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
	private ApplicationContext appContext;

	// Under test
	private RmiAutomatedExporter rmiAutoExporter;
	// Also under test
	private RmiProxyFactory rmiProxyFactory;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		LocalProperties.set(USE_JMS_EVENTS, "true"); // Test using JMS events, prevents Corba exceptions
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
		LocalProperties.clearProperty(USE_JMS_EVENTS);
		LocalProperties.unsetActiveMQBrokerURI();
		LocalProperties.clearProperty(RMI_PORT_PROPERTY);
		LocalProperties.clearProperty("gda.server.host");
	}

	@Before
	public void setUp() throws Exception {
		// Make objects under test
		rmiAutoExporter = new RmiAutomatedExporter();
		rmiProxyFactory = new RmiProxyFactory();
		MockitoAnnotations.initMocks(this);
	}

	@After
	public void tearDown() throws Exception {
		rmiAutoExporter.shutdown();
		// Clean out the Finder
		Finder.getInstance().removeAllFactories();
	}

	@Test
	public void testObjectCanBeExportedAndImported() throws Exception {
		Map<String, Findable> objects = new HashMap<>();
		objects.put("testObj", new TestScannable("testObj"));
		when(appContext.getBeansOfType(Findable.class)).thenReturn(objects);

		rmiAutoExporter.setApplicationContext(appContext);
		// Do the exports
		rmiAutoExporter.afterPropertiesSet();

		// Do the imports
		rmiProxyFactory.afterPropertiesSet();

		final Scannable foundScannable = Finder.getInstance().find("testObj");
		assertThat(foundScannable, is(notNullValue()));
		assertThat(foundScannable, isA(Scannable.class)); // Check type is that of @ServiceInterface
	}

	@Test
	public void testObjectCanBeFoundByType() throws Exception {
		Map<String, Findable> objects = new HashMap<>();
		objects.put("testObj", new TestScannable("testObj"));
		when(appContext.getBeansOfType(Findable.class)).thenReturn(objects);

		rmiAutoExporter.setApplicationContext(appContext);
		// Do the exports
		rmiAutoExporter.afterPropertiesSet();

		// Do the imports
		rmiProxyFactory.afterPropertiesSet();

		final Map<String, Scannable> foundScannables = Finder.getInstance().getFindablesOfType(Scannable.class);
		assertThat(foundScannables.size(), is(equalTo(1)));
		assertThat(foundScannables.get("testObj"), is(notNullValue()));
	}

	@Test
	public void testEventsCanBeReceivedByClientProxies() throws Exception {
		Map<String, Findable> objects = new HashMap<>();

		final TestScannable testScannable = new TestScannable("testScannable");
		objects.put("testScannable", testScannable);
		when(appContext.getBeansOfType(Findable.class)).thenReturn(objects);

		rmiAutoExporter.setApplicationContext(appContext);
		// Do the exports
		rmiAutoExporter.afterPropertiesSet();

		// Do the imports
		rmiProxyFactory.afterPropertiesSet();

		final Scannable foundScannable = Finder.getInstance().find("testScannable");

		// Latch to ensure the observer is notified before the test ends.
		final CountDownLatch latch = new CountDownLatch(1);

		foundScannable.addIObserver((source, arg) -> {
			// The source you have here is actually the proxy so the "real" source is lost.
			// Not sure if this is a problem
 			assertThat(arg , is(equalTo("event")));
			latch.countDown();
		});

		// Send an event from the "server" side
		testScannable.sendEvent("source", "event");

		// Wait for the observer to be notified
		assertThat("No event was received in 5 seconds", latch.await(5, SECONDS)); // If not done in 5 sec the test will fail
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
