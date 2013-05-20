package org.opengda.detector.electronanalyser.event;

import java.io.Serializable;

import org.opengda.detector.electronanalyser.scan.RegionScannable;

/**
 * Finish Event for broadcast by {@link RegionScannable} objects to inform IObervers of the region comption status. used to update the Table Viewer of
 * {@link SequenceView}
 * 
 */
public class RegionStatusEvent implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6492439515246163536L;

	public static enum Status {
		READY, RUNNING, ABORTED, COMPLETED, ERROR
	}

	String regionId;
	public String getRegionId() {
		return regionId;
	}

	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}

	Status status;

	public RegionStatusEvent(String regionId, Status status) {
		this.regionId = regionId;
		this.status = status;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

}
