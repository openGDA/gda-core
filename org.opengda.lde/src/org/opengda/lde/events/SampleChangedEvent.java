package org.opengda.lde.events;

import java.io.Serializable;

public class SampleChangedEvent implements Serializable {

	private String sampleID;

	public SampleChangedEvent(String sampleID) {
		this.sampleID=sampleID;
	}

	public String getSampleID() {
		return sampleID;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 4451390940925281529L;

}
