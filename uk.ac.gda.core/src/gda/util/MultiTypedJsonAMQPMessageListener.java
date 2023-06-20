/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package gda.util;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import javax.jms.Destination;

import uk.ac.diamond.mq.ISessionService;
import uk.ac.diamond.osgi.services.ServiceProvider;

public class MultiTypedJsonAMQPMessageListener extends MultiTypedJsonMessageListener {

	@Override
	protected Destination getTopic(String topicName) throws IOException, TimeoutException {
		return ServiceProvider.getService(ISessionService.class).declareAMQPTopic(topicName);
	}

}
