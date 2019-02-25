package uk.ac.diamond.daq.experiment.plan;

import static uk.ac.diamond.daq.experiment.api.plan.event.EventConstants.EXPERIMENT_PLAN_TOPIC;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.status.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.experiment.api.plan.IExperimentRecord;
import uk.ac.diamond.daq.experiment.api.plan.IPlan;
import uk.ac.diamond.daq.experiment.api.plan.event.PlanStatusBean;
import uk.ac.diamond.daq.experiment.api.plan.event.SegmentRecord;
import uk.ac.diamond.daq.experiment.api.plan.event.TriggerEvent;
import uk.ac.diamond.daq.experiment.api.plan.event.TriggerRecord;


/**
 * Keeps track of what's going on during an {@link IPlan} run
 * by managing a {@link PlanStatusBean} and publishing updates
 * to topic {@code EventContants.EXPERIMENT_PLAN_TOPIC}.
 */
public class ExperimentRecord implements IExperimentRecord {
	
	private static final String BROADCASTING_ERROR_MESSAGE = "Error broadcasting experiment plan event";
	private static final Logger logger = LoggerFactory.getLogger(ExperimentRecord.class);
	private static IEventService eventService;
	
	private final PlanStatusBean bean;
	private IPublisher<PlanStatusBean> publisher;
	
	protected ExperimentRecord(String planName) {
		bean = new PlanStatusBean();
		bean.setName(planName);
		bean.setStatus(Status.PREPARING);
	}
	
	public ExperimentRecord() {
		bean = new PlanStatusBean();
	}
	
	public void setDriverNameAndProfile(String driverName, String profileName) {
		bean.setDriverName(driverName);
		bean.setDriverProfile(profileName);
	}
	
	protected void start() {
		try {
			URI activeMqUri = new URI(LocalProperties.getActiveMQBrokerURI());
			publisher = eventService.createPublisher(activeMqUri, EXPERIMENT_PLAN_TOPIC);
		} catch (URISyntaxException e) {
			logger.error("Error setting up experiment plan publisher. GUI updates will not work!", e);
		}
		
		bean.setStatus(Status.RUNNING);
		// no need to broadcast now: activation of first segment is eminent
	}
	
	protected void complete() {
		bean.setStatus(Status.COMPLETE);
		broadcast();
		
		if (publisher != null) {
			try {
				publisher.disconnect();
			} catch (EventException e) {
				logger.error("Error disconnecting publisher", e);
			}
		}
	}

	protected void segmentActivated(String segmentName) {
		bean.getSegments().add(new SegmentRecord(segmentName));
		broadcast();
	}
	
	protected void segmentComplete(String segmentName, double terminationSignal) {
		getSegmentRecord(segmentName).terminated(terminationSignal);
	}
	
	protected void triggerOccurred(String triggerName) {
		bean.setLastTrigger(triggerName);
		broadcast();
	}
	
	protected void triggerComplete(String triggerName, TriggerEvent event) {
		final TriggerRecord record = bean.getTriggers().stream()
				.filter(t -> t.getTriggerName().equals(triggerName))
				.findFirst().orElse(new TriggerRecord(triggerName));
		
		record.addEvent(event);
		if (!bean.getTriggers().contains(record)) bean.getTriggers().add(record);
		broadcast();
	}
	
	public List<SegmentRecord> getSegmentRecords() {
		return bean.getSegments();
	}
	
	public List<TriggerRecord> getTriggerRecords() {
		return bean.getTriggers();
	}
	
	public SegmentRecord getSegmentRecord(String segmentName) {
		return getSegmentRecords().stream()
					.filter(a -> a.getSegmentName().equals(segmentName))
					.findFirst()
					.orElseThrow(()->new IllegalArgumentException("No record of segment '" + segmentName + "' found"));
	}
	
	public TriggerRecord getTriggerRecord(String triggerName) {
		return getTriggerRecords().stream()
				.filter(a -> a.getTriggerName().equals(triggerName))
				.findFirst()
				.orElseThrow(()->new IllegalArgumentException("No record of trigger '" + triggerName + "' found"));
	}
	
	public String summary() {
		StringBuilder summary = new StringBuilder("Summary:\n");
		
		for (SegmentRecord segment : getSegmentRecords()) {
			summary.append("Segment '").append(segment.getSegmentName())
				.append("' activate between ").append(segment.getStartTime())
				.append(" and ").append(segment.getEndTime())
				.append("\n");
		}
		
		for (TriggerRecord trigger : getTriggerRecords()) {
			summary.append("Trigger '").append(trigger.getTriggerName()).append("' fired at the following times:\n");
			for (TriggerEvent event : trigger.getEvents()) {
				summary.append(event.getTimestamp()).append('\n');
			}
		}
		
		return summary.toString();
	}
	
	private void broadcast() {
		if (publisher != null) {
			try {
				publisher.broadcast(bean);
			} catch (EventException e) {
				logger.error(BROADCASTING_ERROR_MESSAGE, e);
			}
		}
	}
	
	// For OSGi use / testing only!
	public void setEventService(IEventService eventService) {
		ExperimentRecord.eventService = eventService;
	}
}
