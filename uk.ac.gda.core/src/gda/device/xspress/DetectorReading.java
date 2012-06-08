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

package gda.device.xspress;

/**
 * Class to represent readings from an Xspress detector element.
 */

public class DetectorReading {
	private Detector detector;

	private int originalWindowed;

	private int total;

	private int resets;

	private int acc;

	private int windowed;

	/**
	 * @param detector
	 * @param total
	 * @param resets
	 * @param acc
	 * @param originalWindowed
	 */
	public DetectorReading(Detector detector, int total, int resets, int acc, int originalWindowed) {
		this.detector = detector;
		this.total = total;
		this.resets = resets;
		this.acc = acc;
		this.originalWindowed = originalWindowed;

		this.windowed = detector.relinearize(total, resets, acc, originalWindowed);
	}

	/**
	 * @return windowed counts
	 */
	public int getWindowed() {
		return windowed;
	}

	/**
	 * @return total counts
	 */
	public int getTotal() {
		return total;
	}

	/**
	 * @return the detector
	 */
	public Detector getDetector() {
		return detector;
	}

	/**
	 * @return the original windowed
	 */
	public int getOriginalWindowed() {
		return originalWindowed;
	}

	/**
	 * @return the acc
	 */
	public int getAcc() {
		return acc;
	}

	/**
	 * @return number of resets
	 */
	public int getResets() {
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
}
