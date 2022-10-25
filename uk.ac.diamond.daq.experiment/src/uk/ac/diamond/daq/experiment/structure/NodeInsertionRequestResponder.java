/*-
 * Copyright © 2022 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

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
