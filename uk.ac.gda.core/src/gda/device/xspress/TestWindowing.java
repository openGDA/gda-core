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
 * Temporary class for testing.
 */
public class TestWindowing {

	/**
	 * @param data
	 * @param windowStart
	 * @param windowEnd
	 * @return sum for window
	 */
	public static double windowData(double[] data, int windowStart, int windowEnd) {
		double sum = 0.0;

		for (int i = windowStart; i < windowEnd; i++) {
			sum = sum + data[i];
		}

		return sum;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// String filename = "/dls/i18/data/2007/xspress/mca32332dat";
		String filename = "/dls/i18/tmp/mca47321dat";
		int frameNumber = 0;
		int numberOfDetectors = 1;
		long[] data;
		double[] windowedTotals = new double[numberOfDetectors];

		for (int k = 0; k < numberOfDetectors; k++) {
			data = Xspress2Utilities.interpretDataFile(filename, frameNumber, k);

			for (int i = 0; i < data.length; i++) {
				if (data[i] > 0) {
					System.out.println(data[i]);
				}

			}

			windowedTotals[k] = windowData(Xspress2Utilities.sumGrades(data, 0, 15), 1, 4000);
		}

		for (int i = 0; i < numberOfDetectors; i++) {
			System.out.println("Detector[" + i + "]=" + windowedTotals[i]);
		}

	}

}
