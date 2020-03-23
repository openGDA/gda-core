package uk.ac.diamond.daq.experiment.structure;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IRequestHandler;
import org.eclipse.scanning.server.servlet.AbstractResponderServlet;

import uk.ac.diamond.daq.experiment.api.structure.ExperimentStructureJobRequest;

/**
 * Responds to all experiment-related NeXus job requests (i.e. children of {@link ExperimentStructureJobRequest}).
 */
public class ExperimentStructureJobResponder extends AbstractResponderServlet<ExperimentStructureJobRequest> {
	
	private final ExperimentStructureJobHandlerFactory factory = new ExperimentStructureJobHandlerFactory();

	@Override
	public IRequestHandler<ExperimentStructureJobRequest> createResponder(ExperimentStructureJobRequest request,
			IPublisher<ExperimentStructureJobRequest> response) throws EventException {
		
		try {
			ExperimentStructureJobHandler<ExperimentStructureJobRequest> handler = factory.getHandler(request);
			handler.setBean(request);
			handler.setPublisher(response);
			return handler;
		} catch (InstantiationException | IllegalAccessException e) {
			throw new EventException("Could not handle request", e);
		}
		
	}
}
