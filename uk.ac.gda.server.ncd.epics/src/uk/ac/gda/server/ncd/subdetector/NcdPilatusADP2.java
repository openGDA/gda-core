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

package uk.ac.gda.server.ncd.subdetector;

import gda.device.DeviceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * This class is for the P2 2M (30Hz)
 * ideally the different timing requirements could be configured in spring
 */
@ServiceInterface(INcdSubDetector.class)
public class NcdPilatusADP2 extends NcdPilatusAD {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(NcdPilatusADP2.class);

	@Override
	protected DeviceException checkTiming(double shortestFrame, double longestFrame, double shortestWait,
			double longestWait) {

		if (shortestWait < 0.003) {
			return new DeviceException("Readout time too short for " + getName()
					+ ". Need more than 3ms.");
		}
		if ((shortestWait + shortestFrame) < 0.033) {
			return new DeviceException("Cycle time too short for " + getName()
					+ ". Need more than 33ms live and dead time combined.");
		}
		if ((shortestWait < 0.013) && ((longestFrame - shortestFrame) > 0.001 && (longestWait - shortestWait) > 0.001)) {
			return new DeviceException("Cannot vary trigger times with fast read out on "
					+ getName() + ". Choose longer dead time.");
		}
		return null;
	}
}