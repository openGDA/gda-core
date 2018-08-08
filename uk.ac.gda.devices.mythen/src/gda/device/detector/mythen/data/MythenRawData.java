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

package gda.device.detector.mythen.data;

/**
 * Represents a line in a Mythen {@code .raw} file.
 */
public class MythenRawData {
	
	private int channel;
	
	private int count;
	
	/**
	 * Creates a line using the specified channel and count.
	 * 
	 * @param channel the channel number
	 * @param count the channel count
	 */
	public MythenRawData(int channel, int count) {
		this.channel = channel;
		this.count = count;
	}
	
	/**
	 * Returns the channel number.
	 * 
	 * @return the channel number
	 */
	public int getChannel() {
		return channel;
	}
	
	/**
	 * Returns the channel count.
	 * 
	 * @return the channel count
	 */
	public int getCount() {
		return count;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + channel + ", " + count + ")";
	}

}
