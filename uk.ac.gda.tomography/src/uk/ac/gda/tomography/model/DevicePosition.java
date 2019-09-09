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

package uk.ac.gda.tomography.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import uk.ac.gda.tomography.base.serializer.DevicePositionSerializer;

@JsonSerialize(using=DevicePositionSerializer.class)
public class DevicePosition<E> {

	private final String name;
	private final E position;

	public DevicePosition(String name, E position) {
		super();
		this.name = name;
		this.position = position;
	}

	public String getName() {
		return name;
	}

	public E getPosition() {
		return position;
	}
}
