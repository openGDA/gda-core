package org.opengda.lde.events;

import java.io.Serializable;

public class NewDataFileEvent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7577614640069236857L;
	private String filename;
//	private ContentType type;
	
	public NewDataFileEvent(String filename) {
		this.filename=filename;
	}

	public String getFilename() {
		return filename;
	}
}
