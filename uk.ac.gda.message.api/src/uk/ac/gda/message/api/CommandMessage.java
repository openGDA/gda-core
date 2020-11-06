package uk.ac.gda.message.api;

import java.io.Serializable;

public class CommandMessage extends GDAMessage {

	private static final long serialVersionUID = -4571882147090199575L;

	private int priority = 0;

	public CommandMessage(Serializable sourceToken, String message) {
		super(sourceToken, GDAMessageCategory.COMMAND,message);
	}

	public CommandMessage(Serializable sourceToken) {
		super(sourceToken, GDAMessageCategory.COMMAND);
	}

	public CommandMessage() {
		super(null, GDAMessageCategory.COMMAND);
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

}
