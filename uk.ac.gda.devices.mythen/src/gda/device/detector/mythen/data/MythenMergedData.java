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
 * Represents a line in a Mythen merged {@code .dat} file - count and error are changed to double after rebin or summation.
 */
public class MythenMergedData {
	
	private double angle;
	private double count;
	private double error;
	private double channel;
	
	/**
	 * Creates a line using the specified angle, count and error.
	 * 
	 * @param angle the angle, in degrees
	 * @param count the channel count
	 */
	public MythenMergedData(double angle, double count, double error, double channel) {
		this.angle = angle;
		this.count = count;
		this.error = error;
		this.channel = channel;
	}
	
	/**
	 * Returns the angle.
	 * 
	 * @return the angle
	 */
	public double getAngle() {
		return angle;
	}
	
	/**
	 * Returns the channel count.
	 * 
	 * @return the channel count
	 */
	public double getCount() {
		return count;
	}
	
	public double getError() {
		return error;
	}
	
	public double getChannel() {
		return channel;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + angle + ", " + count + ", " + error + ", " + channel +")";
	}

}
