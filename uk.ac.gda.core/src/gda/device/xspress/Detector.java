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

import java.text.NumberFormat;
import java.util.StringTokenizer;

/**
 * Class which holds the data (window, gain etc) for a single detector element in an Xspress or Xspress2 system.
 * Communication with the real detector element is left in the XspressSystem class because it makes sense to speak to
 * all detector elements at once sometimes.
 */
public class Detector {
	// Each detector knows its own number (counting from 0). This has little
	// use within the software but helps to make the configuration file
	// human
	// readable and writeable.
	private int number;

	// The windowStart and windowEnd are the start and end channel numbers
	// of the
	// data which should be summed to produce fluorescence counts.
	private int windowStart;

	private int windowEnd;

	// Gain, offset and deadTime are parameters which are usually determined
	// by
	// the detector group when the system is set up. The values supplied
	// should
	// be hand edited into the configuration file. The gain and offset can
	// be
	// changed subsequently using the XspressPanel or Xspress2Panel. The
	// deadTime
	// has no meaning for Xspress2 detectors.
	private double gain;

	private double offset;

	private double deadTime;

	/**
	 * XspressSystem creates an ArrayList<Detector> of Detectors by reading a file line by line and using this
	 * constructor.
	 * 
	 * @param lineFromFile
	 *            a line read from a file
	 */
	public Detector(String lineFromFile) {
		StringTokenizer strTok = new StringTokenizer(lineFromFile, " \n");

		number = Integer.valueOf(strTok.nextToken()).intValue();
		windowStart = Integer.valueOf(strTok.nextToken()).intValue();
		windowEnd = Integer.valueOf(strTok.nextToken()).intValue();
		gain = Double.valueOf(strTok.nextToken()).doubleValue();
		offset = Double.valueOf(strTok.nextToken()).doubleValue();
		deadTime = Double.valueOf(strTok.nextToken()).doubleValue();
	}

	/**
	 * @param number
	 *            number
	 * @param windowStart
	 * @param windowEnd
	 * @param gain
	 * @param deadTime
	 * @param offset
	 */
	public Detector(int number, int windowStart, int windowEnd, double gain, double deadTime, double offset) {
		this.number = number;
		this.windowStart = windowStart;
		this.windowEnd = windowEnd;
		this.gain = gain;
		this.deadTime = deadTime;
		this.offset = offset;
	}

	/**
	 * XspressSystem uses this to write the Detectors back to the file.
	 * 
	 * @return the string representation of this detector
	 */
	@Override
	public String toString() {
		NumberFormat nf = NumberFormat.getInstance();
		String lineForFile = "";
		nf.setMaximumFractionDigits(6);

		lineForFile = lineForFile + number + " " + windowStart + " ";
		lineForFile = lineForFile + windowEnd + " " + nf.format(gain) + " ";
		lineForFile = lineForFile + nf.format(offset) + " " + deadTime;

		return lineForFile;
	}

	/**
	 * Sets the window.
	 * 
	 * @param windowStart
	 * @param windowEnd
	 */
	public void setWindow(int windowStart, int windowEnd) {
		this.windowStart = windowStart;
		this.windowEnd = windowEnd;
	}

	/**
	 * @return windowStart
	 */
	public int getWindowStart() {
		return windowStart;
	}

	/**
	 * @return windowEnd
	 */
	public int getWindowEnd() {
		return windowEnd;
	}

	/**
	 * @return gain
	 */
	public double getGain() {
		return gain;
	}

	/**
	 * @return deadTime
	 */
	public double getDeadTime() {
		return deadTime;
	}

	/**
	 * @return offset
	 */
	public double getOffset() {
		return offset;
	}

	/**
	 * @return number
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * Rescales the given counts to take into account dead time etc and creates a DetectorReading with the values - not
	 * used by Xspress2Systems.
	 * 
	 * @param total
	 *            the original total counts read from the detector
	 * @param resets
	 *            the number of resets counted
	 * @param acc
	 *            the number of some mysterious electronic things
	 * @param windowed
	 *            the original number of counts in the window
	 * @return the relinearized windowed counts
	 */
	public int relinearize(int total, int resets, int acc, int windowed) {
		// A, B, C, D have no real meaning they are just used to split
		// the rather unwieldy expression into manageable parts.Contact
		// the Detector Group for information about the details of the
		// expression.
		double A;
		double B;
		double C;
		double D;
		double factor;
		double working;
		double deadTimeSquared;
		double deadTimeCubed;
		double bigfactor;

		if (windowed <= 0)
			return (0);

		A = total;
		B = resets;
		C = acc;
		D = windowed;

		factor = (1.0 / (1.0 - B * 1.0e-07));

		A = factor * A;
		C = factor * C;
		D = factor * D;

		deadTimeSquared = deadTime * deadTime;
		deadTimeCubed = deadTime * deadTimeSquared;

		bigfactor = Math.sqrt(4.0 - 20.0 * deadTime * A + 27.0 * deadTimeSquared * A * A);
		bigfactor = bigfactor * Math.sqrt(3.0) / (9.0 * deadTimeCubed);
		bigfactor = bigfactor - 10.0 / (27.0 * deadTimeCubed) + A / deadTimeSquared;
		bigfactor = Math.pow(bigfactor, 1.0 / 3.0);

		working = (bigfactor - 2.0 / (9.0 * deadTimeSquared * bigfactor) + 2.0 / (3.0 * deadTime)) / A;

		working = working * D;

		return (int) working;
	}

	/**
	 * Sets the gain
	 * 
	 * @param gain
	 *            new value
	 */
	public void setGain(double gain) {
		this.gain = gain;
	}

	/**
	 * Sets the offset
	 * 
	 * @param offset
	 *            new value
	 */
	public void setOffset(double offset) {
		this.offset = offset;
	}

	/**
	 * Given an array of multi-channel data calculates the total within the current window - only used by
	 * Xspress2System.
	 * 
	 * @param data
	 *            array of data
	 * @return total within window
	 */
	public double windowData(double[] data) {
		double sum = 0.0;

		for (int i = windowStart; i < windowEnd; i++) {
			sum = sum + data[i];
		}

		return sum;
	}
}
