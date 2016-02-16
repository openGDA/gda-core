package uk.ac.diamond.daq.msgbus.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static uk.ac.diamond.daq.msgbus.MsgBus.BROKER_URL_PROPERTY;
import static uk.ac.diamond.daq.msgbus.MsgBus.publishAsJson;
import static uk.ac.diamond.daq.msgbus.MsgBus.subscribe;
import static uk.ac.diamond.daq.msgbus.MsgBus.unsubscribe;
import gda.configuration.properties.LocalProperties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.eventbus.Subscribe;

public class MsgBusJsonTest {

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

//		shutdown(); // can break subsequent test classes

		// Undo setting of embedded broker URL from setUpClass.
		LocalProperties.set(BROKER_URL_PROPERTY, "");
	}

	private TestJsonMsg lastPublished = null;

	/**
	 * Should contain the only code outside @Before/@After that sets lastPublished.
	 */
	private final class LastPublishedSetter {
		/**
		 * Should be the only code outside @Before/@After that sets lastPublished.
		 */
		@Subscribe public void setLastPublished(TestJsonMsg msg) {
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
		final TestJsonMsg expected = new TestJsonMsg("7", 1);

		// Must not be null.
		assertNotNull(expected);

		// Make it so.
		publishAsJson(expected);

		// Allow some time for lastPublished to be set to expected.
		Thread.sleep(500);

		// Tidy up.
		unsubscribe(setsLastPublished);

		TestJsonMsg actual = lastPublished;

		// Assert lastPublished was at least set to something that was not null by setsLastPublished.
		assertNotNull(actual);

		// And two instances having the same random UUID is almost impossible.
		assertTrue(expected.uuid.equals(actual.uuid));

		// IdentifiableMsg does not implement equals.
		// But if it were to then two instances with equal UUIDs should be equal.
		assertFalse(expected.equals(actual));

		// Therefore (within this test at least) they should not be the same instance (nor inside the same process).
		assertNotSame(expected, actual);

		// It's all good.
	}

}
