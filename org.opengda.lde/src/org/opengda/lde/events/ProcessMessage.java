package org.opengda.lde.events;

import java.io.Serializable;

public class ProcessMessage implements Serializable {

	private String message;

	public ProcessMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 3431824676452707087L;

}
