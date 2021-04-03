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

import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResource;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResourceType;

/**
 * This event is published when a {@link AcquisitionConfigurationResource} is deleted.
 *
 * @author Maurizio Nagni
 */
public class AcquisitionConfigurationResourceDeleteEvent extends AcquisitionConfigurationResourceEvent {

	/**
	 *
	 */
	private static final long serialVersionUID = -454684975840133320L;

	public AcquisitionConfigurationResourceDeleteEvent(Object source, UUID uuid,
			AcquisitionConfigurationResourceType type) {
		super(source, uuid, type);
	}

	public AcquisitionConfigurationResourceDeleteEvent(Object source, UUID uuid) {
		super(source, uuid);
	}
}
