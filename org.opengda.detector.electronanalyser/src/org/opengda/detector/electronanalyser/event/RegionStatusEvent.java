package org.opengda.detector.electronanalyser.event;

import java.io.Serializable;

import org.opengda.detector.electronanalyser.api.SESRegion;

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

	SESRegion.Status status;

	public RegionStatusEvent(String regionId, SESRegion.Status status) {
		this.regionId = regionId;
		this.status = status;
	}

	public SESRegion.Status getStatus() {
		return status;
	}

	public void setStatus(SESRegion.Status status) {
		this.status = status;
	}

}
