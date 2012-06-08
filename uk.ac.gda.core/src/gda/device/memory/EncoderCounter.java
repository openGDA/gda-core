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
import gda.device.Memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A memory class for the HY8513 Hytec Incremental Counter Encoder implemented using DA.Server
 */
public class EncoderCounter extends Gdhist implements Memory {
	
	private static final Logger logger = LoggerFactory.getLogger(EncoderCounter.class);
	
	@Override
	public void clear(int start, int count) {
		logger.error("Not implemented by da.server");
	}

	@Override
	public void clear(int x, int y, int t, int dx, int dy, int dt) {
		logger.error("Not implemented by da.server");
	}

	@Override
	public void setDimension(int[] d) throws DeviceException {
		if (d[1] < 1)
			logger.error("total frames should be greater than 1");
		else if (d[0] < 1 || d[1] > 4)
			logger.error("width is in the range 1 to 4");
		else {
			width = d[0];
			totalFrames = d[1];

			if (handle >= 0 && daServer != null && daServer.isConnected()) {
				daServer.sendCommand("close " + handle);
				handle = -1;
			}

			if (handle >= 0) {
				// otherwise that will happen anyway
				close();
				open();
			}
		}
	}

	@Override
	public int getMemorySize() throws DeviceException {
		int size = 0;
		logger.error("Not implemented by da.server");
		return size;
	}

	@Override
	public int[] getSupportedDimensions() {
		return null;
	}
}