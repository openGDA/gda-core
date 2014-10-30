package org.opengda.lde.events;

import java.io.Serializable;

import org.opengda.lde.model.ldeexperiment.STATUS;

public class SampleStatusEvent implements Serializable {

	private STATUS status;
	private String sampleID;

	public STATUS getStatus() {
		return status;
	}

	public String getSampleID() {
		return sampleID;
	}

	public SampleStatusEvent(String sampleID, STATUS status) {
		this.sampleID=sampleID;
		this.status=status;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -4877172988489454686L;

}
