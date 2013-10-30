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

package gda.device.detector.xspress;

import uk.ac.gda.beans.xspress.DetectorElement;

/**
 * Class to represent readings from an Xspress detector element.
 */

public class DetectorReading {
	private DetectorElement detector;

	private long originalWindowed;

	private long total;

	private long resets;

	private long acc;

	private long windowed;

	/**
	 * @param detector
	 * @param total
	 * @param resets
	 * @param acc
	 * @param originalWindowed
	 */
	public DetectorReading(DetectorElement detector, long total, long resets, long acc, long originalWindowed) {
		this.detector = detector;
		this.total = total;
		this.resets = resets;
		this.acc = acc;
		this.originalWindowed = originalWindowed;
	}

	/**
	 * @param detector the detector
	 * @param data an array of detector scaler readings
	 */
	public DetectorReading(DetectorElement detector, int[] data) {
		this.detector = detector;
		this.total = data[0];
		this.resets = data[1];
		this.originalWindowed = data[2];
		this.acc = data[3];
		// Now remove the sign extension caused by casting from int to long as the data
		// from the detector is really 32bit unsigned, but Java's int is 32bit signed!!
		if (total < 0) total = (total << 32) >>> 32;
		if (resets < 0) resets = (resets << 32) >>> 32;
		if (originalWindowed < 0) originalWindowed = (originalWindowed << 32) >>> 32;
		if (acc < 0) acc = (acc << 32) >>> 32;
	
	} 
	/**
	 * @return windowed counts
	 */
	public long getWindowed() {
		return windowed;
	}

	/**
	 * @return total counts
	 */
	public long getTotal() {
		return total;
	}

	/**
	 * @return the detector
	 */
	public DetectorElement getDetector() {
		return detector;
	}

	/**
	 * @return the original windowed
	 */
	public long getOriginalWindowed() {
		return originalWindowed;
	}

	/**
	 * @return the acc
	 */
	public long getAcc() {
		return acc;
	}

	/**
	 * @return number of resets
	 */
	public long getResets() {
		return resets;
	}

	/**
	 * @return array of total, acc, resets and windowed
	 */
	public double[] toArray() {
		double[] values = new double[4];
		values[0] = getTotal();
		values[1] = getAcc();
		values[2] = getResets();
		values[3] = getWindowed();
		return values;
	}

	@Override
	public String toString() {
		return "total " + total + " resets " + resets + " acc " + acc + " original windowed " + originalWindowed
				+ " rescaled windowed " + windowed;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (acc ^ (acc >>> 32));
		result = prime * result + ((detector == null) ? 0 : detector.hashCode());
		result = prime * result + (int) (originalWindowed ^ (originalWindowed >>> 32));
		result = prime * result + (int) (resets ^ (resets >>> 32));
		result = prime * result + (int) (total ^ (total >>> 32));
		result = prime * result + (int) (windowed ^ (windowed >>> 32));
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
		DetectorReading other = (DetectorReading) obj;
		if (acc != other.acc)
			return false;
		if (detector == null) {
			if (other.detector != null)
				return false;
		} else if (!detector.equals(other.detector))
			return false;
		if (originalWindowed != other.originalWindowed)
			return false;
		if (resets != other.resets)
			return false;
		if (total != other.total)
			return false;
		if (windowed != other.windowed)
			return false;
		return true;
	}
}
