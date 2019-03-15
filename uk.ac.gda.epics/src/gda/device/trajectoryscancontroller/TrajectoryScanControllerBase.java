/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package gda.device.trajectoryscancontroller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import gda.factory.FindableBase;

/**
 * TrajecoryScan base class implementation
 * @since 3/7/2017
 */
public abstract class TrajectoryScanControllerBase extends FindableBase implements TrajectoryScanController {

	protected List<String> axisNames = Arrays.asList("A", "B", "C", "U", "V", "W", "X", "Y", "Z");
	protected List<String> motorNames = Arrays.asList("M1", "M2", "M3", "M4", "M5", "M6", "M7", "M8");

	/** Conversion factor from seconds to trajectory scan time units */
	private int timeConversionFromSecondsToPmacUnits = 1000000;

	private int maxProfilePointsPerBuild = 1500;

	@Override
	public void setAxisNames(String[] axisNames) {
		this.axisNames = Arrays.asList(axisNames);
	}

	@Override
	public List<String> getAxisNames() {
		return axisNames;
	}

	@Override
	public void setMotorNames(String[] motorNames) {
		this.motorNames = Arrays.asList(motorNames);
	}

	@Override
	public List<String> getMotorNames() {
		return motorNames;
	}

	public boolean profileBuiltOk() throws IOException {
		return getBuildStatus() == Status.SUCCESS
				&& getAppendStatus() == Status.SUCCESS
				&& getBuildState() == State.DONE
				&& getAppendState() == State.DONE;
	}


	@Override
	public int getTimeConversionFromSecondsToDeviceUnits() {
		return timeConversionFromSecondsToPmacUnits;
	}

	public void setTimeConversionFromSecondsToDeviceUnits(int timeConversionFromSecondsToPmacUnits) {
		this.timeConversionFromSecondsToPmacUnits = timeConversionFromSecondsToPmacUnits;
	}

	@Override
	public int getMaxPointsPerProfileBuild() {
		return maxProfilePointsPerBuild;
	}

	public void setMaxPointsPerProfileBuild(int maxProfilePointsPerBuild) {
		this.maxProfilePointsPerBuild = maxProfilePointsPerBuild;
	}
}
