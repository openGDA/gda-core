/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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
package gda.device.detector.nexusprocessor.roistats;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROIList;
import org.eclipse.january.dataset.Slice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
/**
 * Contain the data required to represent a region of interest obtained from the plotting system for use in
 * {@link RoiStatsProcessor}.
 */
public class RegionOfInterest {
	public static final double MAX_ROTATION_ANGLE = 1e-6;

	private static final Logger logger = LoggerFactory.getLogger(RegionOfInterest.class);

	/** This is the name of the roi (with spaces removed) used to prefix the data names */
	private final String name;
	private final Slice[] slice;
	private final int x;
	private final int y;
	private final int width;
	private final int height;
	private final double angle;

	/**
	 * Create a {@link RegionOfInterest} from a {@link RectangularROI}.
	 * <p>
	 * TODO consider if {@link RectangularROI} may already be sufficient to replace this class.
	 * @param roi
	 */
	public RegionOfInterest(RectangularROI roi) {
		// Remove spaces from roi name
		name = roi.getName().replace(" ", "_");
		// As the slice[] is [y, x], the indexing here is 0 for y, 1 for x
		x = roi.getIntPoint()[0];
		y = roi.getIntPoint()[1];
		width = roi.getIntLengths()[0];
		height =  roi.getIntLengths()[1];
		angle = roi.getAngleDegrees();
		slice = getSliceFromRoi(roi);
	}

	private Slice[] getSliceFromRoi(RectangularROI roi) {
		if(roi.getAngleDegrees() > MAX_ROTATION_ANGLE) {
			return new Slice[] {};
		}
		int roiX = roi.getIntPoint()[0];
		int roiY = roi.getIntPoint()[1];
		int roiWidth = roi.getIntLengths()[0];
		int roiHeight = roi.getIntLengths()[1];
		// Slice is (y, x) since Dataset will be row-major
		return new Slice[] { new Slice(roiY, roiY + roiHeight, 1), new Slice(roiX, roiX + roiWidth, 1) };
	}

	public String getName() {
		return name;
	}

	/**
	 * Note this will be have been empty if this ROI has been rotated as the values would correspond to the original position, not the rotated one.
	 * @return slice array is ordered [y, x] to match Dataset indexing
	 */
	public Slice[] getSlice() {
		return slice;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public double getAngle() {
		return angle;
	}

	public int getArea() {
		return width * height;
	}

	/**
	 * Utility to get regions from the plotting system.
	 * @param plotName plot get regions from
	 * @return list of regions adapted to {@link RegionOfInterest}
	 */
	public static List<RegionOfInterest> getRoisForPlot(String plotName) {
		try {
			final GuiBean bean = SDAPlotter.getGuiBean(plotName);
			final Object roiListObj = bean.get(GuiParameters.ROIDATALIST);
			if (roiListObj instanceof RectangularROIList roiList) {
				return roiList.stream()
						.filter(IROI::isPlot) // isPlot corresponds to the "Active" property in the GUI (not visible)
						.sorted(Comparator.comparing(RectangularROI::getName))
						.map(RegionOfInterest::new)
						.toList();
			} else {
				// It is null or not rectangular rois
				logger.warn("No rois defined");
			}
		} catch (Exception e) {
			logger.error("Could not get gui bean for plot: {}", plotName, e);
		}
		return Collections.emptyList();
	}
}
