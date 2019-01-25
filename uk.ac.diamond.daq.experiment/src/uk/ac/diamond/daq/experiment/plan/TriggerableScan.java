package uk.ac.diamond.daq.experiment.plan;

import java.net.UnknownHostException;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.scan.ScanningException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.experiment.api.plan.Triggerable;

public class TriggerableScan implements Triggerable {
	
	private static final Logger logger = LoggerFactory.getLogger(TriggerableScan.class);
	
	private final QueuePreventingScanSubmitter scanSubmitter;
	private final ScanRequest<?> scanRequest;
	private final boolean important;
	
	TriggerableScan(ScanRequest<?> scanRequest, boolean important, IEventService eventService) {
		scanSubmitter = new QueuePreventingScanSubmitter();
		scanSubmitter.setEventService(eventService);
		this.scanRequest = scanRequest;
		this.important = important;
	}

	@Override
	public void trigger() {
		try {
			final ScanBean scanBean = new ScanBean(scanRequest);
			if (important) {
				scanSubmitter.submitImportantScan(scanBean);
			} else {
				scanSubmitter.submitScan(scanBean);
			}		
		} catch (UnknownHostException | EventException e) {
			logger.error("Error submitting scan request", e);
		} catch (ScanningException e) {
			logger.error("Scan submission rejected due to non-empty queue", e);
		}
	}

}
