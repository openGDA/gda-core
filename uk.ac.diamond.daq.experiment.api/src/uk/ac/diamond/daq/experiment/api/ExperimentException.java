package uk.ac.diamond.daq.experiment.api;

/**
 * Unchecked wrapper for checked exceptions within an experiment
 */
public class ExperimentException extends RuntimeException {
	
	private static final long serialVersionUID = -9119433201611365660L;

	public ExperimentException() {
		super();
	}
	
	public ExperimentException(String message) {
		super(message);
	}
	
	public ExperimentException(Throwable cause) {
		super(cause);
	}
	
	public ExperimentException(String message, Throwable cause) {
		super(message, cause);
	}

}
