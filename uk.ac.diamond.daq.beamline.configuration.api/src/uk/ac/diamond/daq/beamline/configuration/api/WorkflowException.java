package uk.ac.diamond.daq.beamline.configuration.api;

public class WorkflowException extends Exception {
	private static final long serialVersionUID = -3746881016157505112L;

	public WorkflowException(String message, Throwable cause) {
		super(message, cause);
	}

	public WorkflowException(String message) {
		super(message);
	}
}
