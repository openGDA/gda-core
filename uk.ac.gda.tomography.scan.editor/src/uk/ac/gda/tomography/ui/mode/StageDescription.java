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

package uk.ac.gda.tomography.ui.mode;

import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gda.device.IScannableMotor;
import uk.ac.gda.tomography.base.serializer.IScannableMotorDeserializer;
import uk.ac.gda.tomography.base.serializer.IScannableMotorSerializer;
import uk.ac.gda.tomography.model.DevicePosition;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "stage")
@JsonSubTypes({ @Type(value = GTSStage.class, name = "GTS"), @Type(value = TR6Stage.class, name = "TR6") })
public interface StageDescription {

	enum StageDevices {
		MOTOR_STAGE_X, MOTOR_STAGE_Y, MOTOR_STAGE_Z, MOTOR_STAGE_ROT_Y, MOTOR_CAMERA_Z, MALCOLM_TOMO;
	}

	enum Stage {
		GTS, TR6
	}

	@JsonSerialize(contentUsing = IScannableMotorSerializer.class)
	@JsonDeserialize(contentUsing = IScannableMotorDeserializer.class)
	public Map<StageDevices, IScannableMotor> getMotors();

	public Stage getStage();

	@JsonIgnore()
	public Set<DevicePosition<Double>> getMotorsPosition();

	public Map<String, String> getMetadata();
}
