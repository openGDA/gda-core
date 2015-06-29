package org.opengda.lde.events;

import java.io.Serializable;

public class DataReductionWarnEvent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3739888996277216427L;

	private String filename;
	private String cause;

	private String sampleid;

	public DataReductionWarnEvent(String sampleid, String filename, String cause) {
		this.sampleid=sampleid;
		this.filename=filename;
		this.cause=cause;
	}
	public String getFilename() {
		return filename;
	}
	public String getCause() {
		return cause;
	}
	public String getSampleid() {
		return sampleid;
	}
}
