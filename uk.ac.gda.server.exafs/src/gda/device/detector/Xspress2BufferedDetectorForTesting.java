/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.device.detector;

import gda.device.DeviceException;


/**
 * Version of Xspress2BufferedDetector which takes a software trigger. For system testing.
 */
public class Xspress2BufferedDetectorForTesting extends Xspress2BufferedDetector implements SimulatedBufferedDetector {
	@Override
	public void setContinuousMode(boolean on) throws DeviceException {
		isContinuousMode = on;

		if (on) {
			// The da.server tfg generate command expects cycles (integer) frames(integer) deadTime (seconds, double)
			// liveTime (seconds, double) pause (integer). The incoming time is in mS.
			double totalExptTime = continuousParameters.getTotalTime();
			double timePerPoint = totalExptTime / continuousParameters.getNumberDataPoints();
			xspress2system.getDaServer().sendCommand(
					"tfg generate 1 " + continuousParameters.getNumberDataPoints() + " 0.001 " + timePerPoint / 1000.
							+ " 1");
		}

	}

	@Override
	public void addPoint() throws DeviceException {
		xspress2system.getDaServer().sendCommand("tfg cont");	
	}

}
