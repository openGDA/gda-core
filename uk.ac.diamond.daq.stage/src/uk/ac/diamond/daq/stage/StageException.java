package uk.ac.diamond.daq.stage;

import java.io.Serializable;

public class StageException extends Exception implements Serializable {
	private static final long serialVersionUID = 6153366997665827533L;

	public StageException(String message) {
		super(message);
	}

	public StageException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
