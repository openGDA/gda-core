/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package org.eclipse.scanning.api.device.models;

import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;

/**
 * This class defines a method to configure the chunk size of a detector
 * and a method to find the last good point scanned based on this value.
 *
 * The point to seek will be the nearest multiple of the buffer size.
 * For instance, if the buffer size is 10 and the scan was paused at the
 * step 44, the returned value will be 40.
 */
public class BufferSeekStrategy implements SeekStrategy {

	private int bufferSize;
	private BufferSizeProvider bufferSizeProvider;

	public BufferSeekStrategy(BufferSizeProvider bufferSizeProvider) {
		this.bufferSizeProvider = bufferSizeProvider;
	}

	@Override
	public void configure() throws MalcolmDeviceException {
		bufferSize = bufferSizeProvider.getBufferSize();
		if (bufferSize < 1) {
			throw new MalcolmDeviceException("Error getting chunk size");
		}
	}

	/**
	 * Get the last multiple of the chunk size
	 */
	@Override
	public int getPointToSeek(int scanPoint) {
		return scanPoint - (scanPoint % bufferSize);
	}
}
