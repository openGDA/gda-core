package uk.ac.diamond.daq.experiment.structure;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IRequestHandler;
import org.eclipse.scanning.server.servlet.AbstractResponderServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.ac.diamond.daq.experiment.api.structure.NodeFileCreationRequest;

/**
 * Responds to all experiment-related NeXus job requests (i.e. children of
 * {@link ExperimentStructureJobRequest}).
 */

@Component
public class ExperimentStructureJobResponder extends AbstractResponderServlet<NodeFileCreationRequest> {

	@Autowired
	public ExperimentStructureJobResponder(
			@Value("${experiment.structure.job.request.topic:uk.ac.diamond.daq.experiment.structure.job.request.topic}") String requestTopic,
			@Value("${experiment.structure.job.response.topic:uk.ac.diamond.daq.experiment.structure.job.response.topic}") String responseTopic) {
		super(requestTopic, responseTopic);
	}

	@Override
	public IRequestHandler<NodeFileCreationRequest> createResponder(NodeFileCreationRequest request,
			IPublisher<NodeFileCreationRequest> publisher) throws EventException {
		return new NodeFileCreator(request, publisher);
	}
}
