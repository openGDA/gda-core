package org.opengda.detector.electronanalyser.event;

import java.io.Serializable;

import org.opengda.detector.electronanalyser.scan.RegionScannable;

/**
 * Region change event for broadcasting by {@link RegionScannable} to objects to inform IObervers of the region setting  used to update the Table Viewer of
 * {@link SequenceView}
 * 
 */

public class RegionChangeEvent implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8033679475460737514L;
	private String regionId;
	private String regionName;
	private String message;

	public String getMessage() {
		return message;
	}

	public RegionChangeEvent(String regionID, String reginName) {
		this.regionId = regionID;
		this.regionName=reginName;
	}

	/**
	 * @return Returns the regionId.
	 */
	public String getRegionId() {
		return regionId;
	}

	/**
	 * @param regionId
	 *            The regionId to set.
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	public Object getRegionName() {
		return this.regionName;
	}

}
