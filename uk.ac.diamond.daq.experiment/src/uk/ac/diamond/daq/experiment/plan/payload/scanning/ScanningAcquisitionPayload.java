package uk.ac.diamond.daq.experiment.plan.payload.scanning;

import uk.ac.diamond.daq.experiment.api.plan.Payload;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;

public class ScanningAcquisitionPayload implements Payload {
	
	private final ScanningAcquisition scan;
	private QueueResolution queueResolution;
	
	
	public ScanningAcquisitionPayload(ScanningAcquisition scan, QueueResolution queueResolution) {
		this.scan = scan;
		this.queueResolution = queueResolution;
	}


	public ScanningAcquisition getScan() {
		return scan;
	}


	public QueueResolution getQueueResolution() {
		return queueResolution;
	}
	
	
	public void setQueueResolution(QueueResolution queueResolution) {
		this.queueResolution = queueResolution;
	}

}
