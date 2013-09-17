/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.epics;

import uk.ac.gda.server.ncd.subdetector.ScalingAndOffset;
import gda.device.DeviceException;
import gda.device.scannable.EpicsScannable;

public class ScaledAmplifiedMonitor extends EpicsScannable {
	ScalingAndOffset scalingAndOffset;
	
	public ScalingAndOffset getScalingAndOffset() {
		return scalingAndOffset;
	}

	public void setScalingAndOffset(ScalingAndOffset scalingAndOffset) {
		this.scalingAndOffset = scalingAndOffset;
	}

	@Override
	public void asynchronousMoveTo(Object externalPosition) throws DeviceException {
		throw new DeviceException("I do not move");
	}
	
	@Override
	public Object getPosition() throws DeviceException {
		double value = (Double) super.rawGetPosition();
		if (scalingAndOffset == null)
			return value;
		return scalingAndOffset.getOffset() + value * scalingAndOffset.getScaling();
	}
}