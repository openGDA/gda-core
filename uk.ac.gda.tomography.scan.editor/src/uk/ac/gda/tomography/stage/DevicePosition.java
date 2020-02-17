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

package uk.ac.gda.tomography.stage;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gda.device.IScannableMotor;
import uk.ac.gda.tomography.stage.enumeration.StageDevice;
import uk.ac.gda.tomography.stage.serializer.DevicePositionDeserializer;
import uk.ac.gda.tomography.stage.serializer.DevicePositionSerializer;

/**
 * Represents the position of a {@link IScannableMotor}
 * @param <E>
 *
 * @author Mauizio Nagni
 */
@JsonSerialize(using=DevicePositionSerializer.class)
@JsonDeserialize(using=DevicePositionDeserializer.class)
public class DevicePosition<E> {

	/**
	 * The device connected with this position
	 */
	private final StageDevice stageDevice;
	/**
	 * The device position
	 */
	private final E position;

	public DevicePosition(StageDevice stageDevice, E position) {
		super();
		this.stageDevice = stageDevice;
		this.position = position;
	}

	public StageDevice getStageDevice() {
		return stageDevice;
	}

	public E getPosition() {
		return position;
	}
}
