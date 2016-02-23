/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.gda.eventbus;

import javax.jms.ConnectionFactory;

import uk.ac.gda.eventbus.api.IGDAEventBus;
import uk.ac.gda.eventbus.api.IGDAEventBusFactory;

public class EventBusFactory implements IGDAEventBusFactory {

	private ConnectionFactory connectionFactory = null;
	private String destinationName = null;
	private boolean isDestinationTopicElseQueue = true;

	public EventBusFactory() {}

	public EventBusFactory(ConnectionFactory connectionFactory,
			String destinationName, boolean isDestinationTopicElseQueue) {
		this.connectionFactory = connectionFactory;
		this.destinationName = destinationName;
		this.isDestinationTopicElseQueue = isDestinationTopicElseQueue;
	}

	public IGDAEventBus createBus(
		String identifier,
		ConnectionFactory connectionFactory,
		String destinationName,
		boolean isDestinationTopicElseQueue) {
		
		return new GDAEventBus(identifier,connectionFactory,destinationName,isDestinationTopicElseQueue);
	}
	@Override
	public IGDAEventBus getBus() {
		return this.getBus("default");
	}

	@Override
	public IGDAEventBus getBus(String identifier) {
		return createBus(identifier,connectionFactory,destinationName,isDestinationTopicElseQueue);

	}

	public ConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	public String getDestinationName() {
		return destinationName;
	}

	@Override
	public Boolean isConfigured() {
		return (null!=connectionFactory) && (null!=destinationName);
	}

	public boolean isDestinationTopicElseQueue() {
		return isDestinationTopicElseQueue;
	}

	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	public void setDestinationName(String destinationName) {
		this.destinationName = destinationName;
	}

	public void setDestinationTopicElseQueue(boolean isDestinationTopicElseQueue) {
		this.isDestinationTopicElseQueue = isDestinationTopicElseQueue;
	}

}
