package uk.ac.gda.client.logpanel.view;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.swt.SWT;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import uk.ac.gda.test.helpers.swt.SWTTestBase;

/**
 * Tests for the logpanel
 */
public class LogpanelTest extends SWTTestBase {

	private Logpanel logpanel;

	@BeforeClass
	public static void initializeRealm() {
		class TestingRealm extends Realm {

			public TestingRealm() {
				super();
				Realm.setDefault(this);
			}

			@Override
			public boolean isCurrent() {
				// Would probably be quite dangerous outside a limited testing context!
				return true;
			}
		}
		@SuppressWarnings("unused")
		Realm testingRealm = new TestingRealm();
	}

	@Before
	public void setUp() throws Exception {
		logpanel = new Logpanel(shell, SWT.NONE);
		logpanel.setMinLogLevel(Level.INFO);
		logpanel.setSize(shell.getClientArea().width, shell.getClientArea().height);
		flushUIEventQueue();
	}

	@After
	public void tearDown() throws Exception {
		logpanel = null;
	}

	@Test
	public void loggingEventShouldBeAddedToLoggingEventsList() throws Exception {
		List<?> loggingEvents = logpanel.getInput();
		int originalSize = loggingEvents.size();
		logpanel.addLoggingEvent(new TestLoggingEvent(Level.INFO));
		flushUIEventQueue();
		assertThat(loggingEvents.size(), is(equalTo(originalSize + 1)));
	}

	// Add @Test annotation manually to run the performance tests
	// They should not be included in the standard automatic testing
	public void fullPerformanceTestAtWarnLevel() throws Exception {
		System.out.println();
		System.out.println("Testing with min log level WARN");
		logpanel.setMinLogLevel(Level.WARN);
		runPerformanceTest();
	}

	// Add @Test annotation manually to run the performance tests
	// They should not be included in the standard automatic testing
	public void fullPerformanceTestAtInfoLevel() throws Exception {
		System.out.println();
		System.out.println("Testing with min log level INFO");
		logpanel.setMinLogLevel(Level.INFO);
		runPerformanceTest();
	}

	// Add @Test annotation manually to run the performance tests
	// They should not be included in the standard automatic testing
	public void fullPerformanceTestAtAllLevel() throws Exception {
		System.out.println();
		System.out.println("Testing with min log level ALL");
		logpanel.setMinLogLevel(Level.ALL);
		runPerformanceTest();
		runPerformanceTest();
		runPerformanceTest();
	}

	private void runPerformanceTest() throws Exception {
		final int messagesToSend = 10000;
		for (int messages = 1; messages <= messagesToSend; messages++) {
			if (messages % 200 == 0) {
				logpanel.addLoggingEvent(new TestLoggingEvent(Level.WARN));
			} else if (messages % 20 == 0) {
				logpanel.addLoggingEvent(new TestLoggingEvent(Level.INFO));
			} else {
				logpanel.addLoggingEvent(new TestLoggingEvent(Level.DEBUG));
			}
		}

		final long startTime = System.currentTimeMillis();
		int uiEvents = flushUIEventQueue();
		final long timeToUpdateUi = System.currentTimeMillis() - startTime;

		String message = String.format("%5d logging messages sent;%5d UI events processed in %4d milliseconds;%7.3f milliseconds per message",
				messagesToSend, uiEvents, timeToUpdateUi, (double) timeToUpdateUi / messagesToSend);
		System.out.println(message);
	}

	/**
	 * Process all waiting UI tasks by running the event loop until the event queue is empty
	 *
	 * @return the number of events processed
	 * @throws TimeoutException
	 *             if the event queue is not empty after a minute of running events
	 */
	protected int flushUIEventQueue() throws TimeoutException {
		final long endTime = System.currentTimeMillis() + 60000; // one minute
		// loop until all UI update events have been processed
		int count = 0;
		while (display.readAndDispatch()) {
			count++;
			if (System.currentTimeMillis() > endTime) {
				throw new TimeoutException("Timed out while trying to flush UI event queue");
			}
		}
		return count;
	}
}

class TestLoggingEvent implements ILoggingEvent {

	private static volatile int count = 0;

	private final Level level;
	private final int number;

	public TestLoggingEvent(Level level) {
		this.level = level;
		this.number = ++count;
	}

	@Override
	public Object[] getArgumentArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StackTraceElement[] getCallerData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFormattedMessage() {
		return "Message " + number;
	}

	@Override
	public Level getLevel() {
		return level;
	}

	@Override
	public LoggerContextVO getLoggerContextVO() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLoggerName() {
		return TestLoggingEvent.class.getName();
	}

	@Override
	public Map<String, String> getMDCPropertyMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Marker getMarker() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> getMdc() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getThreadName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IThrowableProxy getThrowableProxy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getTimeStamp() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean hasCallerData() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void prepareForDeferredProcessing() {
		// TODO Auto-generated method stub

	}
}
