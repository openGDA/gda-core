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

package uk.ac.gda.client.experimentdefinition;

import org.eclipse.scanning.api.event.IEventService;

import gda.commandqueue.Command;

/**
 * Holder for an {@link IEventService}. Required to submit {@link Command}s onto the
 * new GDA9 queue.
 */
public class EventServiceHolder {

	private static IEventService eventService;

	/**
	 * For use by OSGi DS
	 * @param service
	 */
	public static void setEventService(IEventService service) {
		eventService = service;
	}

	public static IEventService getEventService() {
		return eventService;
	}

}
