package uk.ac.diamond.daq.experiment.api.structure;

import org.eclipse.scanning.api.event.IdBean;
import org.eclipse.scanning.api.event.status.Status;

/**
 * Super class for all NeXus job requests pertaining to the experiment structure.
 * A status and message are provided to let handlers communicate success/failure/whatever
 * to the requesters.
 * <p>
 * Each concrete implementation must have a handler mapped in {@link ExperimentStructureJobHandlerFactory}
 */
public abstract class ExperimentStructureJobRequest extends IdBean {

	private Status status = Status.NONE;
	private String message;


	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExperimentStructureJobRequest other = (ExperimentStructureJobRequest) obj;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		return status == other.status;
	}

	private static final long serialVersionUID = -630570479593234894L;

}
