/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

import java.util.Collections;
import java.util.List;

/**
 * 
 */
public class PlotServerROISelectionProvider implements RectangularROIProvider{
	
	final private int maximumActiveRois;
	
	final private String roiGroupName;

	public PlotServerROISelectionProvider(String roiGroupName, int maximumActiveRois) {
		this.roiGroupName = roiGroupName;
		this.maximumActiveRois = maximumActiveRois;
	}

	/**
	 * Returns a list of active Rois. This will have no more than maximumActiveRois elements.
	 * @return a list of active rois.
	 */
	public List<RectangularROI> getActiveRoiList() {
		return Collections.emptyList();
	}

	@Override
	public RectangularROI getROI(int index) throws IndexOutOfBoundsException {
		if (index >= maximumActiveRois) {
			throw new IndexOutOfBoundsException("Maximum index is: " + maximumActiveRois);
		}
		
		List<RectangularROI> roiList = getActiveRoiList();
		if (index >= roiList.size()) {
			return null;
		}
		return roiList.get(index);
	}

}
