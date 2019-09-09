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

package uk.ac.gda.tomography.base;

import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gda.device.IScannableMotor;
import uk.ac.gda.tomography.base.serializer.DeviceSerializer;
import uk.ac.gda.tomography.controller.IncompleteModeException;
import uk.ac.gda.tomography.model.DevicePosition;

public interface TomographyMode {

	public enum TomographyDevices {
		MOTOR_STAGE_X, MOTOR_STAGE_Y, MOTOR_STAGE_Z, MOTOR_STAGE_ROT_Y, MOTOR_CAMERA_Z, MALCOLM_TOMO;
	}

	public enum Stage {
		DEFAULT, TR6;
	}

	@JsonSerialize(contentUsing = DeviceSerializer.class)
	public Map<TomographyDevices, IScannableMotor> getMotors() throws IncompleteModeException;
	public Stage getStage();
	@JsonIgnore()
	public Set<DevicePosition<Double>> getMotorsPosition();
	public Map<String, String> getMetadata();
}
