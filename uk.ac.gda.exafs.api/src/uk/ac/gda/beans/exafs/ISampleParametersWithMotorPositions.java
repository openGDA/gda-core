/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.beans.exafs;

import java.util.List;

/**
 * Extension of {@link ISampleParameters} that also provides access to collection of {@link SampleParameterMotorPosition}s.
 */
public interface ISampleParametersWithMotorPositions extends ISampleParameters {
	public static final String MOTOR_POSITION_GETTER_NAME = "getSampleParameterMotorPosition";

	public SampleParameterMotorPosition getSampleParameterMotorPosition(String scannableName);
	public List<SampleParameterMotorPosition> getSampleParameterMotorPositions();
	public void setSampleParameterMotorPositions(List<SampleParameterMotorPosition> sampleParameterMotorPositions);
	public void addSampleParameterMotorPosition(SampleParameterMotorPosition sampleParameterMotorPosition);
}
