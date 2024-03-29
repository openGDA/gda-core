package uk.ac.diamond.daq.experiment.scans.mapping;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IJobQueue;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class QueuePreventingScanSubmitterTest {

	private QueuePreventingScanSubmitter scanSubmitter;
	private List<ScanBean> submissionQueue;
	private List<ScanBean> runningAndCompleted;

	@Mock
	private IEventService eventService;
	@Mock
	private ISubmitter<StatusBean> submitter;
	@Mock
	private IJobQueue<ScanBean> consumer;

	@Before
	public void setUp() throws EventException {
		when(eventService.createSubmitter(any(), anyString())).thenReturn(submitter);

		// Queue starts empty
		submissionQueue = new ArrayList<>();
		runningAndCompleted = new ArrayList<>();

		when(consumer.getSubmissionQueue()).thenReturn(submissionQueue);
		when(consumer.getRunningAndCompleted()).thenReturn(runningAndCompleted);
		doReturn(consumer).when(eventService).getJobQueue(anyString());

		scanSubmitter = new QueuePreventingScanSubmitter();
		scanSubmitter.setEventService(eventService);
	}

	@Test
	public void canSubmitScanToEmptyQueue() throws Exception {
		ScanBean scan = getTestScanBean();
		scanSubmitter.submitScan(scan);
		verify(submitter).submit(scan);
	}

	@Test
	public void submittingScanToNonEmptyQueueThrows() throws Exception {
		submissionQueue.add(getTestScanBean());
		ScanBean scanBean = getTestScanBean();

		var e = assertThrows(ScanningException.class, () -> scanSubmitter.submitScan(scanBean));
		assertThat(e.getMessage(),
				is("Could not submit request for '" + scanBean.getName() + "' because another scan is ongoing"));
	}

	@Test
	public void canSubmitImportantScanToEmptyQueue() throws Exception {
		ScanBean scan = getTestScanBean();
		scanSubmitter.submitImportantScan(scan);
		verify(submitter).submit(scan);
	}

	@Test
	public void importantSubmissionClearsSubmittedList() throws Exception {
		submissionQueue.add(getTestScanBean());
		submissionQueue.add(getTestScanBean());

		scanSubmitter.submitImportantScan(getTestScanBean());

		verify(consumer, times(2)).terminateJob(any());
	}

	@Test
	public void importantSubmissionAbortsRunningScan() throws Exception {
		ScanBean runningScan = getTestScanBean();
		runningScan.setStatus(Status.RUNNING);
		runningAndCompleted.add(runningScan);

		scanSubmitter.submitImportantScan(getTestScanBean());

		verify(consumer).terminateJob(runningScan);
	}

	private ScanBean getTestScanBean() throws UnknownHostException {
		IScanPointGeneratorModel model = mock(IScanPointGeneratorModel.class);
		CompoundModel compoundModel = mock(CompoundModel.class);
		when(compoundModel.getModels()).thenReturn(Arrays.asList(model));

		ScanRequest scanRequest = mock(ScanRequest.class);
		doReturn(compoundModel).when(scanRequest).getCompoundModel();
		ScanBean bean = new ScanBean(scanRequest);
		bean.setName("test scan");
		return bean;
	}
}
