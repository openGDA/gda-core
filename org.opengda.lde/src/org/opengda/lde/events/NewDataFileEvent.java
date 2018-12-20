package org.opengda.lde.events;

import java.io.Serializable;

public class NewDataFileEvent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7577614640069236857L;
	private String filename;
//	private ContentType type;
	private String sampleID;
	
	public String getSampleID() {
		return sampleID;
	}

	public NewDataFileEvent(String sampelID, String filename) {
		this.filename=filename;
		this.sampleID=sampelID;
	}

	public String getFilename() {
		return filename;
	}
}
