package uk.ac.diamond.daq.experiment.structure;

import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IRequestHandler;

import uk.ac.diamond.daq.experiment.api.structure.ExperimentStructureJobRequest;

/**
 * Base class for all handlers of {@link ExperimentStructureJobRequest}.
 * <p>
 * Notes for implementors:
 * <ul>
 * <li> Concrete children <b>must</b> have a no-argument constructor
 * <li> If a request cannot be handled, set the request bean's status and/or message to reflect this and return the request
 * </ul>
 */
public abstract class ExperimentStructureJobHandler<T extends ExperimentStructureJobRequest> implements IRequestHandler<T> {
	
	private T bean;
	private IPublisher<T> publisher;
	
	public void setBean(T bean) {
		this.bean = bean;
	}

	@Override
	public T getBean() {
		return bean;
	}
	
	public void setPublisher(IPublisher<T> publisher) {
		this.publisher = publisher;
	}

	@Override
	public IPublisher<T> getPublisher() {
		return publisher;
	}
	
}
