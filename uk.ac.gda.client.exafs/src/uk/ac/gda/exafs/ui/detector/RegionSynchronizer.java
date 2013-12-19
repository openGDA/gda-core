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

package uk.ac.gda.exafs.ui.detector;

import org.dawnsci.plotting.api.region.IROIListener;
import org.dawnsci.plotting.api.region.ROIEvent;

public class RegionSynchronizer implements IROIListener {
	private DetectorElementComposite detectorElementComposite;
	private Boolean updatingAfterROIDrag = null;
	protected Counts counts;
	private int selectedElementIndex = 0;
	private int[][][] detectorData;
	private double start;
	private double end;
	
	public RegionSynchronizer(Counts counts){
		this.counts = counts;
	}
	
	public void setDetectorElementComposite(DetectorElementComposite detectorElementComposite) {
		this.detectorElementComposite = detectorElementComposite;
	}

	public void setSelectedElementIndex(int selectedElementIndex){
		this.selectedElementIndex = selectedElementIndex;
	}
	
	public void setDetectorData(int[][][] detectorData) {
		this.detectorData = detectorData;
	}

	@Override
	public void roiDragged(ROIEvent evt) {
	}

	public void setStart(double start) {
		this.start = start;
	}

	public void setEnd(double end) {
		this.end = end;
	}

	public double getStart() {
		return start;
	}

	public double getEnd() {
		return end;
	}

	@Override
	public void roiChanged(ROIEvent evt) {
		if (updatingAfterROIDrag == null) {
			try {
				updatingAfterROIDrag = true;
				detectorElementComposite.getStart().setValue(start);
				detectorElementComposite.getEnd().setValue(end);
				detectorElementComposite.setTotalCounts(counts.getTotalCounts(detectorData));
				detectorElementComposite.setTotalElementCounts(counts.getTotalElementCounts(selectedElementIndex, detectorData));
				
			} finally {
				updatingAfterROIDrag = null;
			}
		}
	}

	@Override
	public void roiSelected(ROIEvent evt) {
	}
}
