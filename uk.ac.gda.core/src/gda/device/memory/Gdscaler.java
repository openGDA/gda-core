/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device.memory;

import gda.device.DeviceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A memory class for the VME Generic data acquisition histogramming memory card implemented using DA.Server
 */
public class Gdscaler extends Gdhist {

	private static final Logger logger = LoggerFactory.getLogger(Gdscaler.class);

	@Override
	protected void open() throws DeviceException {
		String cmd = openCommand;
		handle = -1;

		if (!openCommand.startsWith("gdscaler")) {
			logger.error("Invalid open command for " + getName());
			throw new DeviceException("Gdscaler: Invalid open command for " + this.getName());
		}
		handle = ((Integer) sendAndParse("open", cmd)).intValue();

		if (startupScript != null) {
			String command = "~" + startupScript + " " + width;
			if (height > 1) {
				command += " " + height;
			}
			sendAndParse("startup script", command);
		}
	}
}