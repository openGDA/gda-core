package org.opengda.lde.events;

import java.io.Serializable;

public class DataReductionFailedEvent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7577614640069236857L;
	private String message;
	private String sampleid;

	public DataReductionFailedEvent(String sampleid, String cause) {
		this.sampleid=sampleid;
		this.message=cause;
	}

	public String getMesaage() {
		return message;
	}

	public String getSampleid() {
		return sampleid;
	}

}
