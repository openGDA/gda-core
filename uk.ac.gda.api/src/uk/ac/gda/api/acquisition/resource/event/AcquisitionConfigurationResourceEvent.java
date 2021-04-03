/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.api.acquisition.resource.event;

import java.util.UUID;

import org.springframework.context.ApplicationEvent;

import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResource;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResourceType;

/**
 * Classes extending this one are published on events related to {@link AcquisitionConfigurationResource}.
 * Abstract reflects that it shall be not used as generic event
 *
 * @author Maurizio Nagni
 */
public abstract class AcquisitionConfigurationResourceEvent extends ApplicationEvent {

	/**
	 *
	 */
	private static final long serialVersionUID = -5094032542760128396L;
	private final UUID uuid;
	private final AcquisitionConfigurationResourceType type;

	/**
	 * Instantiates a resource event defining its {@code source} and {@code url} location
	 * @param source the object which published this event
	 * @param uuid the document id
	 */
	protected AcquisitionConfigurationResourceEvent(Object source, UUID uuid, AcquisitionConfigurationResourceType type) {
		super(source);
		this.uuid = uuid;
		this.type = type;
	}


	protected AcquisitionConfigurationResourceEvent(Object source, UUID uuid) {
		this(source, uuid, null);
	}

	public UUID getUuid() {
		return uuid;
	}

	public AcquisitionConfigurationResourceType getType() {
		return type;
	}
}