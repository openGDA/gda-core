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

package gda.device.detector;

/**
 * For detector classes which are to be used in Continuous Scans during partial systems testing (testing hardware but with software triggering) by a
 * SimulatedContinuouslyScannable
 */
public interface SimulatedBufferedDetector extends BufferedDetector {

	/**
	 * Collects another frame of data. This should be stored in memory for retrieval by the readFrames method. The value
	 * from getNumberFrames should be incremented.
	 */
	public void addPoint();
}
