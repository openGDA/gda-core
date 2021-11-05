package uk.ac.diamond.daq.experiment.structure;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IRequestHandler;
import org.eclipse.scanning.server.servlet.AbstractResponderServlet;

import uk.ac.diamond.daq.experiment.api.structure.NodeFileCreationRequest;

/**
 * Responds to {@link NodeFileCreationRequest}).
 */
public class NodeFileCreationRequestResponder extends AbstractResponderServlet<NodeFileCreationRequest> {

	@Override
	public IRequestHandler<NodeFileCreationRequest> createResponder(NodeFileCreationRequest request,
			IPublisher<NodeFileCreationRequest> publisher) throws EventException {
		return new NodeFileCreator(request, publisher);
	}
}
