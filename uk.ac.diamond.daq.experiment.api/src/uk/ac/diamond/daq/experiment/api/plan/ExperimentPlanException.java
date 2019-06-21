package uk.ac.diamond.daq.experiment.api.plan;

public class ExperimentPlanException extends RuntimeException {

	public ExperimentPlanException(Exception cause) {
		super(cause);
	}

	public ExperimentPlanException(String message, Exception cause) {
		super(message, cause);
	}

	public ExperimentPlanException(String message) {
		super(message);
	}

	private static final long serialVersionUID = -6595362754972853437L;

}
