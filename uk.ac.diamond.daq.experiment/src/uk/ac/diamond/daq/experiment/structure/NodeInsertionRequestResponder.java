package uk.ac.diamond.daq.experiment.structure;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IRequestHandler;
import org.eclipse.scanning.server.servlet.AbstractResponderServlet;

import uk.ac.diamond.daq.experiment.api.structure.NodeInsertionRequest;

/**
 * Responds to {@link NodeInsertionRequest}).
 */
public class NodeInsertionRequestResponder extends AbstractResponderServlet<NodeInsertionRequest> {

	@Override
	public IRequestHandler<NodeInsertionRequest> createResponder(NodeInsertionRequest request,
			IPublisher<NodeInsertionRequest> publisher) throws EventException {
		return new NodeFileCreator(request, publisher);
	}
}
