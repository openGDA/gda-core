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

package uk.ac.diamond.daq.msgbus.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static uk.ac.diamond.daq.msgbus.MsgBus.publishAsJson;
import static uk.ac.diamond.daq.msgbus.MsgBus.subscribe;
import static uk.ac.diamond.daq.msgbus.MsgBus.unsubscribe;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.eventbus.Subscribe;

import gda.configuration.properties.LocalProperties;

public class MsgBusGsonTest {

	@BeforeClass
	public static void setUpClass() {
		// Assert that JUnit isn't running in dummy mode (at least when running this JUnit test directly from Eclipse)
		assertFalse(LocalProperties.isDummyModeEnabled());

		// Must use embedded ActiveMQ broker for JUnit tests.
		LocalProperties.forceActiveMQEmbeddedBroker();
		// Disable setting of broker URI property above to use with local broker outside process, e.g.:
		// module load activemq; activemq start
	}

	@AfterClass
	public static void tearDownClass() {

//		shutdown(); // can break subsequent test classes

		// Undo setting of embedded broker URL from setUpClass.
		LocalProperties.unsetActiveMQBrokerURI();
	}

	private TestMsg lastPublished = null;

	/**
	 * Should contain the only code outside @Before/@After that sets lastPublished.
	 */
	private final class LastPublishedSetter {
		/**
		 * Should be the only code outside @Before/@After that sets lastPublished.
		 */
		@Subscribe public void setLastPublished(TestMsg msg) {
			lastPublished = msg;
		}
	}

	@Before
	public void setUp() {
		lastPublished = null;
	}

	@After
	public void tearDown() {
		lastPublished = null;
	}

	@Test
	public void testPublishJSON() throws InterruptedException {

		final LastPublishedSetter setsLastPublished = new LastPublishedSetter();

		subscribe(setsLastPublished);

		// Our publication.
		final TestMsg expected = new TestMsg("7", 1);

		// Must not be null.
		assertNotNull(expected);

		// Make it so.
		publishAsJson(expected);

		// Allow some time for lastPublished to be set to expected.
		Thread.sleep(500);

		// Tidy up.
		unsubscribe(setsLastPublished);

		TestMsg actual = lastPublished;

		// Assert lastPublished was at least set to something that was not null by setsLastPublished.
		assertNotNull(actual);

		// And two instances having the same id is almost impossible.
		assertTrue(expected.mid == actual.mid);

		// IdentifiableMsg does not implement equals.
		// But if it were to then two instances with equal UUIDs should be equal.
		assertFalse(expected.equals(actual));

		// Therefore (within this test at least) they should not be the same instance (nor inside the same process).
		assertNotSame(expected, actual);

		// It's all good.
	}

}
