/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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
package uk.ac.diamond.daq.activemq.test;

import uk.ac.diamond.daq.activemq.AbstractActiveMQSessionService;
import uk.ac.diamond.daq.activemq.ConnectionWrapper;
import uk.ac.diamond.daq.activemq.QueueConnectionWrapper;

/**
 * Test implementation of {@code ISessionService} , which makes a new Connection for each requested Session. As Tests
 * should be run in isolation, this prevents tests from running in the same Connection as each other, and potentially
 * closing Connections still in use.
 */
public class TestSessionService extends AbstractActiveMQSessionService {

	@Override
	public void accept(ConnectionWrapper t) {
		// Do nothing, test implementation doesn't track connections
	}

	@Override
	protected ConnectionWrapper getConnection(String brokerUri) {
		// Do nothing, test implementation doesn't track connections
		return createConnection(brokerUri);
	}

	@Override
	protected QueueConnectionWrapper getQueueConnection(String brokerUri) {
		// Do nothing, test implementation doesn't track connections
		return createQueueConnection(brokerUri);

	}

}
