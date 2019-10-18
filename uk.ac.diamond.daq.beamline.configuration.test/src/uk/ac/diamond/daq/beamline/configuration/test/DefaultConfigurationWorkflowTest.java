package uk.ac.diamond.daq.beamline.configuration.test;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import gda.device.DeviceException;
import gda.device.Scannable;
import uk.ac.diamond.daq.beamline.configuration.DefaultConfigurationWorkflow;
import uk.ac.diamond.daq.beamline.configuration.LookupTableBasedItem;
import uk.ac.diamond.daq.beamline.configuration.api.ConfigurationWorkflow;
import uk.ac.diamond.daq.beamline.configuration.api.ScannablePositionLookupService;
import uk.ac.diamond.daq.beamline.configuration.api.WorkflowException;
import uk.ac.diamond.daq.beamline.configuration.api.WorkflowItem;

public class DefaultConfigurationWorkflowTest {

	private Scannable firstMirror;
	private Scannable secondMirror;

	private static final String FIRST_MIRROR = "dodgyMirror";
	private static final String SECOND_MIRROR = "secondMirror";

	private static final double GOOD_VALUE = 20.5;
	private static final double BAD_VALUE = 12.6;

	// regardless of demand, lookupService always returns these
	private static final double FIRST_MIRROR_TARGET = 14.3;
	private static final double SECOND_MIRROR_TARGET = 13.96;
	private static final String FIRST_MIRROR_NAME = "firstMirror";

	private static final String INPUT_PROPERTY = "input";

	private ScannablePositionLookupService lookupService;

	private ConfigurationWorkflow workflow;

	private Properties properties;

	@Before
	public void setup() throws Exception {
		firstMirror = mock(Scannable.class);
		when(firstMirror.getName()).thenReturn(FIRST_MIRROR_NAME);
		secondMirror = mock(Scannable.class);

		lookupService = mock(ScannablePositionLookupService.class);
		when(lookupService.getScannablePositions(GOOD_VALUE,singleton(FIRST_MIRROR))).thenReturn(singletonMap(FIRST_MIRROR, FIRST_MIRROR_TARGET));
		when(lookupService.getScannablePositions(BAD_VALUE, singleton(FIRST_MIRROR))).thenReturn(singletonMap(FIRST_MIRROR, BAD_VALUE));
		doThrow(DeviceException.class).when(firstMirror).asynchronousMoveTo(BAD_VALUE);
		when(lookupService.getScannablePositions(anyDouble(), eq(singleton(SECOND_MIRROR)))).thenReturn(singletonMap(SECOND_MIRROR, SECOND_MIRROR_TARGET));

		WorkflowItem item1 = new LookupTableBasedItem(INPUT_PROPERTY, singletonMap(FIRST_MIRROR, firstMirror), lookupService);
		WorkflowItem item2 = new LookupTableBasedItem(INPUT_PROPERTY, singletonMap(SECOND_MIRROR, secondMirror), lookupService);

		workflow = new DefaultConfigurationWorkflow(asList(item1, item2));

		properties = new Properties();
	}

	@Test
	public void happyMotors() throws Exception {
		runWorkflow(GOOD_VALUE);
		verify(firstMirror).asynchronousMoveTo(FIRST_MIRROR_TARGET);
		verify(secondMirror).asynchronousMoveTo(SECOND_MIRROR_TARGET);
	}

	@Test
	public void sadMotors() throws Exception {
		try {
			runWorkflow(BAD_VALUE);
		} catch (WorkflowException e) {
			verify(secondMirror, times(0)).asynchronousMoveTo(any());
			assertThat(e.getMessage(), containsString("Error moving scannable '" + firstMirror.getName() + "'"));
		}
	}

	@Test
	public void abortedWhileRunning() throws Exception {

		workflow = new DefaultConfigurationWorkflow(asList(new CpuSpinner()));

		Executors.newScheduledThreadPool(1).schedule(()->{
			try {
				workflow.abort();
			} catch (WorkflowException e) {
				fail("Abort is not supposed to throw");
			}
		}, 10, TimeUnit.MILLISECONDS);

		try {
			workflow.start(null);
			fail("Should not terminate nicely");
		} catch (WorkflowException e) {
			assertThat(e.getMessage(), containsString("Stopped by user"));
		}
	}

	private void runWorkflow(double input) throws Exception {
		properties.setProperty(INPUT_PROPERTY, Double.toString(input));
		workflow.start(properties);
	}

	private class CpuSpinner implements WorkflowItem {

		private volatile boolean aborted;

		@Override
		public void start(Properties workflowProperties) throws WorkflowException {
			while (!aborted) { /* spin cpu */ }
		}

		@Override
		public void abort() throws WorkflowException {
			aborted = true;
		}
	}

}
