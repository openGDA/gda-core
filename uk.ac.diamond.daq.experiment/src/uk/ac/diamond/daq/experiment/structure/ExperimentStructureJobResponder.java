package uk.ac.diamond.daq.experiment.structure;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IRequestHandler;
import org.eclipse.scanning.server.servlet.AbstractResponderServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.ac.diamond.daq.experiment.api.structure.ExperimentStructureJobRequest;

/**
 * Responds to all experiment-related NeXus job requests (i.e. children of
 * {@link ExperimentStructureJobRequest}).
 */

@Component
public class ExperimentStructureJobResponder extends AbstractResponderServlet<ExperimentStructureJobRequest> {

	private final ExperimentStructureJobHandlerFactory factory = new ExperimentStructureJobHandlerFactory();

	@Autowired
	public ExperimentStructureJobResponder(
			@Value("${experiment.structure.job.request.topic:uk.ac.diamond.daq.experiment.structure.job.request.topic}") String requestTopic,
			@Value("${experiment.structure.job.response.topic:uk.ac.diamond.daq.experiment.structure.job.response.topic}") String responseTopic) {
		super(requestTopic, responseTopic);
	}

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
