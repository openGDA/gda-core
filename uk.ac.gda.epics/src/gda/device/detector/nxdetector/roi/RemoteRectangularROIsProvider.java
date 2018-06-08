/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package gda.device.detector.nxdetector.roi;

import java.util.List;

import gda.factory.Findable;

/**
 * Interface for use with RMI to allow ROIs to be updated from the client
 *
 * @author James Mudd
 */
public interface RemoteRectangularROIsProvider extends Findable {

	/**
	 * Update the ROIs list on the server, which can the be picked up when a scan is started
	 *
	 * @param rois
	 *            The new list of ROIs
	 */
	public void updateRois(List<RectangularROI<Integer>> rois);

}
