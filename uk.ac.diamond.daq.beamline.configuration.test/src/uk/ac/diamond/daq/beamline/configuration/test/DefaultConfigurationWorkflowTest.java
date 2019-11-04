package uk.ac.diamond.daq.beamline.configuration.test;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.Answer;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.observable.IObserver;
import uk.ac.diamond.daq.beamline.configuration.DefaultConfigurationWorkflow;
import uk.ac.diamond.daq.beamline.configuration.LookupTableBasedItem;
import uk.ac.diamond.daq.beamline.configuration.ProgressInformation;
import uk.ac.diamond.daq.beamline.configuration.api.ConfigurationWorkflow;
import uk.ac.diamond.daq.beamline.configuration.api.ScannablePositionLookupService;
import uk.ac.diamond.daq.beamline.configuration.api.WorkflowUpdate;
import uk.ac.diamond.daq.beamline.configuration.api.WorkflowException;
import uk.ac.diamond.daq.beamline.configuration.api.WorkflowItem;
import uk.ac.diamond.daq.beamline.configuration.api.WorkflowStatus;

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

	private UpdateRecorder observer;

	private ProgressInformation progressInfo;

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

		Map<WorkflowItem, ProgressInformation> contextualisedItems = new LinkedHashMap<>();
		progressInfo = mock(ProgressInformation.class);
		contextualisedItems.put(item1, progressInfo);
		contextualisedItems.put(item2, progressInfo);

		observer = new UpdateRecorder();

		workflow = new DefaultConfigurationWorkflow(contextualisedItems);
		workflow.addIObserver(observer);

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
		runWorkflow(BAD_VALUE);
		verify(secondMirror, times(0)).asynchronousMoveTo(any());
		String failureMessage = observer.updates.stream()
				.filter(event -> event.getStatus() == WorkflowStatus.FAULT)
				.findFirst().orElseThrow(()->new NoSuchElementException("No failures; failures expected"))
				.getMessage();
		assertThat(failureMessage, containsString("Error moving scannable '" + firstMirror.getName() + "'"));
	}

	private void runWorkflow(double input) {
		properties.setProperty(INPUT_PROPERTY, Double.toString(input));
		workflow.start(properties);
	}

	/* testing messages sent */

	@Test
	public void runningStatusReceived() throws WorkflowException {
		ConfigurationWorkflow workflow = new DefaultConfigurationWorkflow(Collections.singletonMap(mock(WorkflowItem.class), mock(ProgressInformation.class)));

		UpdateRecorder observer = new UpdateRecorder();
		workflow.addIObserver(observer);
		workflow.start(null);
		assertThat(observer.updates.size(), is(2)); // one for the start, one for the end
		assertThat(observer.updates.get(0).getStatus(), is(WorkflowStatus.RUNNING));
		assertThat(observer.updates.get(1).getStatus(), is(WorkflowStatus.IDLE));
	}

	@Test
	public void progressInformationReceived() throws WorkflowException {
		String description1 = "Moving DCM";
		String description2 = "Optimising mirrors";
		double percentage1 = 30;
		double percetange2 = 100;
		ProgressInformation info1 = new ProgressInformation(description1, percentage1);
		ProgressInformation info2 = new ProgressInformation(description2, percetange2);

		Map<WorkflowItem, ProgressInformation> items = new LinkedHashMap<>();
		items.put(mock(WorkflowItem.class), info1);
		items.put(mock(WorkflowItem.class), info2);

		ConfigurationWorkflow workflow = new DefaultConfigurationWorkflow(items);

		UpdateRecorder observer = new UpdateRecorder();
		workflow.addIObserver(observer);

		workflow.start(null); // ends immediately

		assertThat(observer.updates.size(), is(3));

		// start of workflow, start of first item
		checkUpdate(observer.updates.get(0), WorkflowStatus.RUNNING, description1, 0);

		// start of second item
		checkUpdate(observer.updates.get(1), WorkflowStatus.RUNNING, description2, percentage1);

		// end of workflow
		checkUpdate(observer.updates.get(2), WorkflowStatus.IDLE, "Workflow complete", percetange2);
	}

	@Test
	public void exceptionInItem() throws Exception {
		String faultMessage = "Couldn't do the thing";
		WorkflowItem faultyItem = mock(WorkflowItem.class);

		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(org.mockito.invocation.InvocationOnMock invocation) throws Throwable {
				throw new WorkflowException(faultMessage);
			};
		}).when(faultyItem).start(any());

		ConfigurationWorkflow workflow = new DefaultConfigurationWorkflow(singletonMap(faultyItem, mock(ProgressInformation.class)));

		UpdateRecorder observer = new UpdateRecorder();
		workflow.addIObserver(observer);

		workflow.start(null);

		assertThat(observer.updates.size(), is(equalTo(2)));
		WorkflowUpdate faultEvent = observer.updates.get(1);
		checkUpdate(faultEvent, WorkflowStatus.FAULT, faultMessage, 0);
	}

	@Test
	public void terminatedByUser() throws Exception {

		ConfigurationWorkflow workflow = new DefaultConfigurationWorkflow(singletonMap(new LatchItem(), mock(ProgressInformation.class)));

		UpdateRecorder observer = new UpdateRecorder();
		workflow.addIObserver(observer);

		Executors.newSingleThreadScheduledExecutor().schedule(workflow::abort, 10, TimeUnit.MILLISECONDS);

		workflow.start(null);

		/* one event for workflow start, one for termination */
		assertThat(observer.updates.size(), is(equalTo(2)));
		checkUpdate(observer.updates.get(1), WorkflowStatus.INTERRUPTED, DefaultConfigurationWorkflow.INTERRUPTED_MESSAGE, 0);
	}

	@Test
	public void errorAborting() throws Exception {
		ConfigurationWorkflow workflow = new DefaultConfigurationWorkflow(singletonMap(new DodgyAbort(), mock(ProgressInformation.class)));

		UpdateRecorder observer = new UpdateRecorder();
		workflow.addIObserver(observer);

		Executors.newSingleThreadScheduledExecutor().schedule(workflow::abort, 10, TimeUnit.MILLISECONDS);

		workflow.start(null);

		/* one event for workflow start, one for termination */
		assertThat(observer.updates.size(), is(equalTo(3)));
		checkUpdate(observer.updates.get(1), WorkflowStatus.FAULT,
				DefaultConfigurationWorkflow.ERROR_WHILE_ABORTING_MESSAGE + DodgyAbort.ERROR_MESSAGE, 0);
		checkUpdate(observer.updates.get(2), WorkflowStatus.INTERRUPTED, DefaultConfigurationWorkflow.INTERRUPTED_MESSAGE, 0);
	}

	private void checkUpdate(WorkflowUpdate updateToCheck, WorkflowStatus expectedStatus, String expectedMessage, double expectedPercentage) {
		assertThat(updateToCheck.getStatus(), is(equalTo(expectedStatus)));
		assertThat(updateToCheck.getMessage(), is(equalTo(expectedMessage)));
		assertThat(updateToCheck.getPercentComplete(), is(equalTo(expectedPercentage)));
	}

	private class UpdateRecorder implements IObserver {

		List<WorkflowUpdate> updates = new ArrayList<>();

		@Override
		public void update(Object source, Object arg) {
			updates.add((WorkflowUpdate) arg);
		}

	}

	/**
	 * start() calls await on a countdown latch;
	 * abort() counts down.
	 */
	private class LatchItem implements WorkflowItem {

		CountDownLatch latch = new CountDownLatch(1);

		@Override
		public void start(Properties workflowProperties) throws WorkflowException {
			try {
				latch.await(100, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				fail("Interrupted!");
			}
		}

		@Override
		public void abort() throws WorkflowException {
			latch.countDown();
		}

	}

	/**
	 * start() blocks, abort() throws
	 */
	private class DodgyAbort extends LatchItem {

		static final String ERROR_MESSAGE = "Help me";
		@Override
		public void abort() throws WorkflowException {
			throw new WorkflowException(ERROR_MESSAGE);
		}
	}

}
