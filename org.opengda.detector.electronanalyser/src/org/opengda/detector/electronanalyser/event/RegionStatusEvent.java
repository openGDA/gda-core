package org.opengda.detector.electronanalyser.event;

import java.io.Serializable;

import org.opengda.detector.electronanalyser.model.regiondefinition.api.STATUS;
import org.opengda.detector.electronanalyser.scan.RegionScannable;

/**
 * Finish Event for broadcast by {@link RegionScannable} objects to inform IObervers of the region completion status. used to update the Table Viewer of
 * {@link SequenceView}
 * 
 */
public class RegionStatusEvent implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6492439515246163536L;

	String regionId;
	public String getRegionId() {
		return regionId;
	}

	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}

	STATUS status;

	private int regionNumber;

	public int getRegionNumber() {
		return regionNumber;
	}

	public RegionStatusEvent(String regionId, STATUS status, int i) {
		this.regionId = regionId;
		this.status = status;
		this.regionNumber=i;
	}

	public STATUS getStatus() {
		return status;
	}

	public void setStatus(STATUS status) {
		this.status = status;
	}

}
