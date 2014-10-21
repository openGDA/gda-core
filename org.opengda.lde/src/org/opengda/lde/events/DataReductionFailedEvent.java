package org.opengda.lde.events;

import java.io.Serializable;

public class DataReductionFailedEvent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7577614640069236857L;
	private String message;
	
	public DataReductionFailedEvent(String message) {
		this.message=message;
	}

	public String getMesaage() {
		return message;
	}
}
