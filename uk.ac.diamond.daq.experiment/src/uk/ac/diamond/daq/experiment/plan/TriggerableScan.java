package uk.ac.diamond.daq.experiment.plan;

import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.IdBean;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;

import uk.ac.diamond.daq.experiment.api.plan.ExperimentPlanException;
import uk.ac.diamond.daq.experiment.api.plan.Triggerable;

public class TriggerableScan implements Triggerable {

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
	public IdBean trigger() {
		try {
			final ScanBean scanBean = new ScanBean(scanRequest);
			if (important) {
				scanSubmitter.submitImportantScan(scanBean);
			} else {
				scanSubmitter.submitScan(scanBean);
			}
			return scanBean;
		} catch (Exception e) {
			throw new ExperimentPlanException(e);
		}
	}

}
