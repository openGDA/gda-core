package org.opengda.lde.events;

import java.io.Serializable;

public class DataFileEvent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3510088542944595591L;
	private String filename;
	public String getFilename() {
		return filename;
	}

	public boolean isCalibration() {
		return isCalibration;
	}

	public String getId() {
		return id;
	}

	private boolean isCalibration;
	private String id;

	public DataFileEvent(String id, boolean isCalibration, String filename) {
		this.id=id;
		this.isCalibration=isCalibration;
		this.filename=filename;
	}

}
