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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Intended to be the server side object providing the {@link RemoteRectangularROIsProvider} interface for clients to update ROIs over RMI.
 *
 * @author James Mudd
 */
public class LiveStreamROIProvider implements RemoteRectangularROIsProvider {

	private static Logger logger = LoggerFactory.getLogger(LiveStreamROIProvider.class);

	/** The list of ROIs which will be updated via RMI by calling the {@link #updateRois(List)} method */
	private final List<RectangularROI<Integer>> roisList = new ArrayList<>();

	public List<RectangularROI<Integer>> getRois() {
		return roisList;
	}

	@Override
	public void updateRois(final List<RectangularROI<Integer>> rois) {
		roisList.clear();
		roisList.addAll(rois);
		logger.debug("Updated ROIs list. Contains {} ROIs", roisList.size());
	}

}
