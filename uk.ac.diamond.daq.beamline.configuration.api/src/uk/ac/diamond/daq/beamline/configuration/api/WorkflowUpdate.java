package uk.ac.diamond.daq.beamline.configuration.api;

import java.io.Serializable;

public class WorkflowUpdate implements Serializable {

	private WorkflowStatus status;
	private String message;
	private double percentComplete;

	public WorkflowUpdate() {
		// no-arg constructor for deserialisation
	}

	public WorkflowUpdate(WorkflowStatus status, String message, double percentComplete) {
		this.status = status;
		this.message = message;
		this.percentComplete = percentComplete;
	}

	public WorkflowStatus getStatus() {
		return status;
	}

	public void setStatus(WorkflowStatus status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public double getPercentComplete() {
		return percentComplete;
	}

	public void setPercentComplete(double percentComplete) {
		this.percentComplete = percentComplete;
	}

	@Override
	public String toString() {
		return "WorkflowEvent [status=" + status + ", message=" + message + ", percentComplete=" + percentComplete
				+ "]";
	}

	private static final long serialVersionUID = 6148201837581346884L;

}