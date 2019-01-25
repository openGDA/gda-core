package uk.ac.diamond.daq.experiment.plan;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.Finder;
import uk.ac.diamond.daq.experiment.api.plan.ITrigger;

/**
 * The goal of this scan submitter is to prevent scans from queueing.
 * This mechanism is useful for handling {@link ITrigger} collisions.
 * Without it, scans would queue and quickly become out of sync with
 * the real events that triggered their submission.
 * <p>
 * When the queue is empty, the submitter will submit a {@link ScanBean}
 * as normal. When the queue is not empty, the behaviour depends on the
 * level of importance assigned to a scan:
 * <ol>
 * <li>{@link #submitScan(ScanBean)} will throw {@link ScanningException}
 * <li>{@link #submitImportantScan(ScanBean)} will clear the queue
 * by aborting running & submitted scans, and then submit the important scan.
 * </ol>
 * 
 */
public class QueuePreventingScanSubmitter {
	
	private static final Logger logger = LoggerFactory.getLogger(QueuePreventingScanSubmitter.class);
	
	private IEventService eventService;
	
	private IConsumer<ScanBean> consumer;
	private ISubmitter<ScanBean> submitter;
	
	
	/**
	 * Submits the given scan if the queue is empty, otherwise throws ScanningException
	 */
	public void submitScan(ScanBean scanBean) throws EventException, ScanningException {
		if (queueIsEmpty()) {
			getSubmitter().submit(scanBean);
		} else {
			throw new ScanningException("Could not submit request for '" + scanBean.getName() + "' because another scan is ongoing");
		}
	}
	
	/**
	 * Submits the given scan, aborting any previously submitted and running scans 
	 */
	public void submitImportantScan(ScanBean scanBean) throws EventException {
		if (!queueIsEmpty()) {
			abortRunningScanAndClearQueue();
			waitWhileScannablesAreBusy(scanBean);
		}
		getSubmitter().submit(scanBean);
	}

	private void waitWhileScannablesAreBusy(ScanBean scanBean) {
		scanBean.getScanRequest().getCompoundModel().getModels().stream()
			.filter(IScanPathModel.class::isInstance).map(IScanPathModel.class::cast)
			.map(IScanPathModel::getScannableNames).flatMap(List::stream)
			.distinct().map(Finder.getInstance()::find)
			.filter(Scannable.class::isInstance).map(Scannable.class::cast)
			.forEach(scannable -> {
				try {
					scannable.waitWhileBusy();
				} catch (DeviceException | InterruptedException e) {
					logger.error("Error encountered while waiting while busy", e);
				}
			});
	}

	private void abortRunningScanAndClearQueue() throws EventException {
		// first request termination on submitted scans
		for (ScanBean submittedJob : getConsumer().getSubmissionQueue()) {
			getConsumer().terminateJob(submittedJob);
		}
		// now for the one that is running (if any)
		Optional<ScanBean> running = getConsumer().getRunningAndCompleted().stream()
					.filter(bean -> bean.getStatus() == Status.RUNNING || bean.getStatus() == Status.PAUSED).findAny();
		if (running.isPresent()) getConsumer().terminateJob(running.get());
		
	}
	
	private boolean queueIsEmpty() {
		try {
			return submissionQueueIsEmpty() && runningOrCompletedAllFinal();
		} catch (EventException e) {
			logger.error("Error reading submission queue", e);
			return false;
		}
	}
	
	private boolean submissionQueueIsEmpty() throws EventException {
		return getConsumer().getSubmissionQueue().isEmpty();
	}
	
	private boolean runningOrCompletedAllFinal() throws EventException {
		return getConsumer().getRunningAndCompleted().stream()
				.map(StatusBean::getStatus).allMatch(Status::isFinal);
	}
	
	private ISubmitter<ScanBean> getSubmitter() throws EventException {
		if (submitter == null) {
			Objects.requireNonNull(eventService, "Event service is not set - check OSGi settings");
			try {
				URI queueServerURI = new URI(LocalProperties.getActiveMQBrokerURI());
				submitter = eventService.createSubmitter(queueServerURI, EventConstants.SUBMISSION_QUEUE);
			} catch (URISyntaxException e) {
				throw new EventException("URI syntax problem", e);
			}
		}
		return submitter;
	}
	
	@SuppressWarnings("unchecked")
	private IConsumer<ScanBean> getConsumer() throws EventException {
		if (consumer == null) { 
			Objects.requireNonNull(eventService, "Event service is not set - check OSGi settings");
			consumer = (IConsumer<ScanBean>) eventService.getConsumer(EventConstants.SUBMISSION_QUEUE);
		}
		return consumer;
	}
	
	/**
	 * For OSGi/unit tests only
	 */
	public void setEventService(IEventService service) {
		this.eventService = service;
	}
	
}
