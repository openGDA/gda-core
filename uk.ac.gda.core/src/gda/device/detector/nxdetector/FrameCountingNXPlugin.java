/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package gda.device.detector.nxdetector;

/**
 * Interface for plugins that need to know the number of frames they receive during a scan or scan line
 */
public interface FrameCountingNXPlugin extends NXPluginBase {

	/**
	 * @param framesCollected
	 *            - frames collected during the scan
	 * @throws Exception
	 */
	void completeCollection(int framesCollected) throws Exception;

	/**
	 * @param framesCollected
	 *            - frames collected during a scan line
	 * @throws Exception
	 */
	void completeLine(int framesCollected) throws Exception;
}
