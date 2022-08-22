package uk.ac.diamond.daq.experiment.plan.payload.scanning;

/**
 * Policy for when a scan is already running at the time of submission
 */
public enum QueueResolution {
	
	/**
	 * A scan is already running, do not add this one to the queue
	 */
	DROP,
	
	/**
	 * This is an important scan, so stop anything else that's running to make room for this one
	 */
	STOP_PREVIOUS_SCANS;	

}
