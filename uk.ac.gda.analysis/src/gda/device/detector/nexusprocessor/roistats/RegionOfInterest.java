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

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.january.dataset.Slice;

/**
 * Contain the data required to represent a region of interest obtained from the
 * plotting system for use in {@link RoiStatsProcessor}.
 */
public class RegionOfInterest {

	/**	This is the name of the roi (with spaces removed) used to prefix the data names */
	private final String namePrefix;
	private final Slice[] slice;
	private final Set<RoiMetadata> roiMetadata;

	public RegionOfInterest(RectangularROI roi) {
		slice = getSliceFromRoi(roi);
		// Remove spaces from roi name
		namePrefix = roi.getName().replace(" ", "_");
		roiMetadata = createRoiMetaData(roi);
	}

	private Set<RoiMetadata> createRoiMetaData(RectangularROI roi) {
		Integer x = slice[0].getStart();
		Integer y = slice[1].getStart();
		Integer width = slice[0].getStop() - slice[0].getStart();
		Integer height = slice[1].getStop() - slice[1].getStart();

		Set<RoiMetadata> data = new LinkedHashSet<>();
		data.add(new RoiMetadata(namePrefix + "_X", "%.0f", x.doubleValue()));
		data.add(new RoiMetadata(namePrefix + "_Y", "%.0f", y.doubleValue()));
		data.add(new RoiMetadata(namePrefix + "_Width", "%.0f", width.doubleValue()));
		data.add(new RoiMetadata(namePrefix + "_Height", "%.0f", height.doubleValue()));
		data.add(new RoiMetadata(namePrefix + "_Angle", "%f", roi.getAngleDegrees()));
		return data;
	}

	private Slice[] getSliceFromRoi(RectangularROI roi) {
		int x = roi.getIntPoint()[0];
		int y = roi.getIntPoint()[1];
		int width = roi.getIntLengths()[0];
		int height = roi.getIntLengths()[1];
		// Slice is (y, x) since Dataset will be row-major
		return new Slice[] {new Slice(y, y + height, 1), new Slice(x, x + width, 1)};
	}

	public String getNamePrefix() {
		return namePrefix;
	}

	public Slice[] getSlice() {
		return slice;
	}


	public Set<RoiMetadata> getRoiMetadata() {
		return roiMetadata;
	}

	/**
	 * Convenience object to contain data for a metadata item belonging to the
	 * region of interest. This is used for setting output names and formats.
	 */
	public static class RoiMetadata {

		private final String name;
		private final String format;
		private final Double data;

		public RoiMetadata(String name, String format, Double data) {
			this.name = name;
			this.format = format;
			this.data = data;
		}

		public String getName() {
			return name;
		}

		public String getFormat() {
			return format;
		}

		public Double getData() {
			return data;
		}
	}


}