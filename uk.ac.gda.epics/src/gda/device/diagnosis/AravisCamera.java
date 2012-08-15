/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.device.diagnosis;

import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.detector.addetector.ADDetector;
import gda.device.scannable.PositionCallableProvider;

import java.util.concurrent.Callable;

public class AravisCamera extends ADDetector implements IAravisCamera, PositionCallableProvider<NexusTreeProvider> {

	private String deviceName;
	@Override
	public Callable<NexusTreeProvider> getPositionCallable() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}
	public void setDeviceName(String devicename) {
		this.deviceName = devicename;
	}
	public String getDeviceName() {
		return deviceName;
	}

}
