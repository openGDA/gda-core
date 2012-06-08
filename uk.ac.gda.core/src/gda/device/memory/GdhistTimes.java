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

package gda.device.memory;

import gda.device.DeviceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GdhistTimes extends Scaler {

	private static final Logger logger = LoggerFactory.getLogger(GdhistTimes.class);

	@Override
	protected void open() throws DeviceException {
		String cmd = openCommand;
		handle = -1;

		if (!openCommand.contains("tfg_times")) {
			logger.error("Invalid open command for " + getName());
			throw new DeviceException("GdhistTimes: Invalid open command for " + this.getName());
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
	
	@Override
	public void clear() throws DeviceException {
	}

	@Override
	public void clear(int start, int count) throws DeviceException {
	}

	@Override
	public void clear(int x, int y, int t, int dx, int dy, int dt) throws DeviceException {
	}

	@Override
	public void start() throws DeviceException {
	}

	@Override
	public void stop() throws DeviceException {
	}

	@Override
	public void output(String file) throws DeviceException {
		String command;
		ensureOpen();

		if (host == null || user == null || password == null || host.equals("") || user.equals("")
				|| password.equals("")) {
			command = "read 0 0 0 " + totalFrames + " " + width + " 1 to-local-file '" + file + "' from "
					+ handle + " float " + remoteEndian;
		} else {
			command = "read 0 0 0 " + totalFrames + " " + width + " 1 to-remote-file '" + file + "' on '"
					+ host + "' user '" + user + "' password '" + password + "' from " + handle + " float "
					+ remoteEndian;
		}
		sendAndParse("output", command);
	}
}