package uk.ac.diamond.daq.msgbus.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static uk.ac.diamond.daq.msgbus.MsgBus.BROKER_URL_PROPERTY;
import static uk.ac.diamond.daq.msgbus.MsgBus.post;
import static uk.ac.diamond.daq.msgbus.MsgBus.publish;
import static uk.ac.diamond.daq.msgbus.MsgBus.subscribe;
import static uk.ac.diamond.daq.msgbus.MsgBus.unsubscribe;
import gda.configuration.properties.LocalProperties;

import java.io.Serializable;
import java.util.UUID;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.diamond.daq.msgbus.MsgBus.Msg;

import com.google.common.eventbus.Subscribe;

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
		LocalProperties.set(BROKER_URL_PROPERTY, "vm://localhost?broker.persistent=false"); // http://activemq.apache.org/how-to-unit-test-jms-code.html
		// Disable setting of MsgBus.BROKER_URL_PROPERTY above to use
		// MsgBus.getBrokerUrlFallbackDefault(), with e.g.
		// module load activemq; activemq start
	}

	@AfterClass
	public static void tearDownClass() {
		// Undo setting of embedded broker URL from setUpClass.
		LocalProperties.set(BROKER_URL_PROPERTY, "");
	}

	private Object lastPublished = null;

	/**
	 * Should contain the only code outside @Before/@After that sets lastPublished.
	 */
	private final class LastPublishedSetter {
		/**
		 * Should be the only code outside @Before/@After that sets lastPublished.
		 */
		@Subscribe public void setLastPublished(Object/*IdentifiableMessage*/ message) {
			lastPublished = message;
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

	/**
	 * Has a randomly generated UUID.
	 *
	 * Does not implement equals.
	 */
	@SuppressWarnings("serial")
	static class IdentifiableMsg implements Msg {
		public final UUID uuid = UUID.randomUUID();
	}

	@Test
	public void testPublishSubscribe() throws InterruptedException {

		final LastPublishedSetter setsLastPublished = new LastPublishedSetter();

		subscribe(setsLastPublished);

		// Our publication.
		final IdentifiableMsg expected = new IdentifiableMsg();

		// Must not be null.
		assertNotNull(expected);

		// Make it so.
		publish(expected);

		// Allow some time for lastPublished to be set to expected.
		Thread.sleep(500);

		// Tidy up.
		unsubscribe(setsLastPublished);

		// Should throw a ClassCastException if lastPublished not set to an IdentifiableMsg by setsLastPublished.
		IdentifiableMsg actual = (IdentifiableMsg) lastPublished;

		// Assert lastPublished was at least set to something that was not null by setsLastPublished.
		assertNotNull(actual);

		System.out.println(expected);
		System.out.println(actual);

		// And two IdentifiableMsg instances having the same random UUID is almost impossible.
		assertTrue(expected.uuid.equals(actual.uuid));

		// IdentifiableMsg does not implement equals.
		// But if it were to then two instances with equal UUIDs should be equal.
		assertFalse(expected.equals(actual));

		// Therefore (within this test at least) they should not be the same instance (nor inside the same process).
		assertNotSame(expected, actual);

		// It's all good.
	}

	@Test
	public void testMsgSerializable() {
		@SuppressWarnings("serial")
		final class ShouldBeSerializable implements Msg {
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
