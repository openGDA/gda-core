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
 * Represents a line in a Mythen {@code .dat} file.
 */
public class MythenProcessedData {
	
	private double angle;
	private int count;
	private int error;
	private int channel =0;
	
	/**
	 * Creates a line using the specified angle, count and error.
	 * 
	 * @param angle the angle, in degrees
	 * @param count the channel count
	 * @param error the error
	 */
	public MythenProcessedData(double angle, int count, int error) {
		this.angle = angle;
		this.count = count;
		this.error = error;
	}
	
	public MythenProcessedData(double angle, int count, int error, int channel) {
		this.channel = channel;
		this.count = count;
		this.angle = angle;
		this.error = error;
	}
	
	public int getChannel() {
		return channel;
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
	public int getCount() {
		return count;
	}
	
	/**
	 * Returns the error.
	 * 
	 * @return the error
	 */
	public int getError() {
		return error;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + angle + ", " + count + ", " + error + ", " + channel + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(angle);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + count;
		result = prime * result + error;
		result = prime * result + channel;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MythenProcessedData other = (MythenProcessedData) obj;
		if (Double.doubleToLongBits(angle) != Double.doubleToLongBits(other.angle))
			return false;
		if (count != other.count)
			return false;
		if (error != other.error)
			return false;
		if (channel != other.channel)
			return false;
		return true;
	}

}
