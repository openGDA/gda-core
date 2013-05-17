package org.opengda.detector.electronanalyser.event;

import java.io.Serializable;

import org.opengda.detector.electronanalyser.scan.RegionScannable;

/**
 * Finish Event for broadcast by {@link RegionScannable} objects to inform IObervers of the region comption status. used to update the Table Viewer of
 * {@link SequenceView}
 * 
 */
public class RegionFinishEvent implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6492439515246163536L;

	public static enum FinishType {
		OK, INTERRUPTED, ERROR
	}

	String regionId;
	public String getRegionId() {
		return regionId;
	}

	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}

	FinishType finishType;

	public RegionFinishEvent(String regionId, FinishType finishType) {
		this.regionId = regionId;
		this.finishType = finishType;
	}

	public FinishType getFinishType() {
		return finishType;
	}

	public void setFinishType(FinishType finishType) {
		this.finishType = finishType;
	}

}
