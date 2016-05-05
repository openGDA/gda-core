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
import static uk.ac.diamond.daq.msgbus.MsgBus.post;
import static uk.ac.diamond.daq.msgbus.MsgBus.publish;
import static uk.ac.diamond.daq.msgbus.MsgBus.subscribe;
import static uk.ac.diamond.daq.msgbus.MsgBus.unsubscribe;

import java.io.Serializable;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.diamond.daq.msgbus.MsgBus.Msg;

import com.google.common.eventbus.Subscribe;

import gda.configuration.properties.LocalProperties;

/**
 * Test methods for {@link uk.ac.diamond.daq.msgbus.MsgBus}.
 * <p>
 * MsgBus methods subscribe (register), unsubscribe (deregister) and post
 * simply delegate to Guava's EventBus which is thoroughly tested by Google.
 * (See also <a href="https://github.com/bbejeck/guava-blog/blob/master/src/test/java/bbejeck/guava/eventbus/EventBusTest.java">
 * bbejeck's EventBusTest on Github</a>.)
 */
public class MsgBusTest {

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

	private Object lastPublished = null;

	/**
	 * Should contain the only code outside @Before/@After that sets lastPublished.
	 */
	private final class LastPublishedSetter {
		/**
		 * Should be the only code outside @Before/@After that sets lastPublished.
		 */
		@Subscribe public void setLastPublished(Object msg) {
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
	public void testPublishSubscribe() throws InterruptedException {

		final LastPublishedSetter setsLastPublished = new LastPublishedSetter();

		subscribe(setsLastPublished);

		// Our publication.
		final TestMsg expected = new TestMsg("Eleventy", 1);

		// Must not be null.
		assertNotNull(expected);

		// Make it so.
		publish(expected);

		// Allow some time for lastPublished to be set to expected.
		Thread.sleep(500);

		// Tidy up.
		unsubscribe(setsLastPublished);

		// Should throw a ClassCastException if lastPublished not set to an TestMsg by setsLastPublished.
		TestMsg actual = (TestMsg) lastPublished;

		// Assert lastPublished was at least set to something that was not null by setsLastPublished.
		assertNotNull(actual);

		// And two TestMsg instances having the same random UUID is almost impossible.
		assertTrue(expected.mid == actual.mid);

		// TestMsg does not implement equals.
		// But if it were to then two instances with equal UUIDs should be equal.
		assertFalse(expected.equals(actual));

		// Therefore (within this test at least) they should not be the same instance (nor inside the same process).
		assertNotSame(expected, actual);

		// It's all good.
	}

	@SuppressWarnings("cast")
	@Test
	public void testMsgSerializable() {
		@SuppressWarnings("serial")
		final class ShouldBeSerializable extends Msg {
			// empty (and suppressing "serial" warnings
		}
		assertTrue(new ShouldBeSerializable() instanceof Serializable);
	}

	/**
	 * Assert publishing null throws an IllegalArgumentException, in the
	 * caller's thread, not later, once the (null) message has been
	 * received from ActiveMQ and is being posted (in a another process);
	 * more informative than letting EventBus throw a NullPointerException.
	 */
	@Test(expected=IllegalArgumentException.class)
	public final void testPublishNull() {
		publish(null);
	}

	/**
	 * Assert posting null throws an IllegalArgumentException, which is
	 * more informative than letting EventBus throw a NullPointerException.
	 */
	@Test(expected=IllegalArgumentException.class)
	public final void testPostNull() {
		post(null);
	}

	/**
	 * Assert subscribing null throws an IllegalArgumentException, which is
	 * more informative than letting EventBus throw a NullPointerException.
	 */
	@Test(expected=IllegalArgumentException.class)
	public final void testSubscribeNull() {
		subscribe(null);
	}

	/**
	 * Assert unsubscribing null throws an IllegalArgumentException, which is
	 * more informative than letting EventBus throw a NullPointerException.
	 */
	@Test(expected=IllegalArgumentException.class)
	public final void testUnsubcribeNull() {
		unsubscribe(null);
	}

}
