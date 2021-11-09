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

package uk.ac.diamond.daq.mapping.region;

import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.CONSTANT;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.ORIENTATION;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.START;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.STOP;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.X;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.Y;

import java.util.Map;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.ILineMappingRegion;
import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;

/**
 * A line region that snaps to horizontal or vertical.
 */
public class SnappedLineMappingRegion extends DefaultCoordinatePCSRegion implements ILineMappingRegion {
	private static final Logger logger = LoggerFactory.getLogger(SnappedLineMappingRegion.class);

	private enum Orientation {
		HORIZONTAL,
		VERTICAL
	}

	private Orientation orientation = Orientation.HORIZONTAL;

	public SnappedLineMappingRegion() {
		super(Map.of(START, 0.0, STOP, 1.0, CONSTANT, 0.0));
	}

	@Override
	public String getName() {
		return "Snapped Line";
	}

	private double getStart() {
		return coordinates.get(START);
	}

	private double getStop() {
		return coordinates.get(STOP);
	}

	private double getConstant() {
		return coordinates.get(CONSTANT);
	}

	@Override
	public IROI toROI() {
		LinearROI roi = new LinearROI();
		roi.setPoint(getxStart(), getyStart());
		roi.setEndPoint(getxStop(), getyStop());
		return roi;
	}

	@Override
	public String whichPlottingRegionType() {
		return RegionType.LINE.toString();
	}

	/**
	 * Overrides {@link IMappingScanRegionShape} to also snap the given ROI to
	 * horizontal or vertical, depending on the longest axis.
	 *
	 * @see IMappingScanRegionShape#updateFromROI(IROI)
	 */
	@Override
	public void updateFromROI(IROI newROI) {
		if (!(newROI instanceof LinearROI)) {
			throw new IllegalArgumentException("Snapped line mapping region can only update from a LinearROI");
		}

		LinearROI roi = (LinearROI) newROI;

		// First save the old orientation value
		Orientation oldOrientation = orientation;
		if (Math.abs(roi.getEndPoint()[X] - roi.getPoint()[X]) > Math.abs(roi.getEndPoint()[Y] - roi.getPoint()[Y])) {
			orientation = Orientation.HORIZONTAL;
			roi.setEndPoint(roi.getEndPoint()[X], roi.getPoint()[Y]);
			updatePropertyValuesAndFire(Map.of(
					START, roi.getPoint()[X],
					STOP, roi.getEndPoint()[X],
					CONSTANT, roi.getPoint()[Y]));
		} else {
			orientation = Orientation.VERTICAL;
			roi.setEndPoint(roi.getPoint()[X], roi.getEndPoint()[Y]);
			updatePropertyValuesAndFire(Map.of(
					START, roi.getPoint()[Y],
					STOP, roi.getEndPoint()[Y],
					CONSTANT, roi.getPoint()[X]));
		}
		firePropertyChange(ORIENTATION, oldOrientation, orientation);
	}

	@Override
	public double getxStart() {
		return orientation == Orientation.HORIZONTAL ? getStart() : getConstant();
	}

	@Override
	public double getyStart() {
		return orientation == Orientation.HORIZONTAL ? getConstant() : getStart();
	}

	@Override
	public double getxStop() {
		return orientation == Orientation.HORIZONTAL ? getStop() : getConstant();
	}

	@Override
	public double getyStop() {
		return orientation == Orientation.HORIZONTAL ? getConstant() : getStop();
	}

	@Override
	public IMappingScanRegionShape copy() {
		final SnappedLineMappingRegion copy = new SnappedLineMappingRegion();
		copy.orientation = orientation;
		return copy;
	}

	@Override
	public void centre(double x0, double y0) {
		double halfLength = Math.abs(getStop() - getStart()) / 2.0;
		int sign = getStart() < getStop() ? 1 : -1;
		switch (orientation) {
		case HORIZONTAL:
			updatePropertyValuesAndFire(Map.of(
					START, x0 - sign * halfLength,
					STOP, x0 + sign * halfLength,
					CONSTANT, y0));
			break;
		case VERTICAL:
			updatePropertyValuesAndFire(Map.of(
					START, y0 - sign * halfLength,
					STOP, y0 + sign * halfLength,
					CONSTANT, x0));
			break;
		default:
			throw new IllegalStateException("Unexpected orientation: " + orientation.toString());
		}
	}

	@Override
	public void updateFromPropertiesMap(Map<String, Object> properties) {
		logger.error("Setting SnappedLineMappingRegion from properties map not currently supported");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(getConstant());
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((orientation == null) ? 0 : orientation.hashCode());
		temp = Double.doubleToLongBits(getStart());
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(getStop());
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SnappedLineMappingRegion other = (SnappedLineMappingRegion) obj;
		if (Double.doubleToLongBits(getConstant()) != Double.doubleToLongBits(other.getConstant()))
			return false;
		if (orientation != other.orientation)
			return false;
		if (Double.doubleToLongBits(getStart()) != Double.doubleToLongBits(other.getStart()))
			return false;
		if (Double.doubleToLongBits(getStop()) != Double.doubleToLongBits(other.getStop())) // NOSONAR for idiomatic consistency
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SnappedLineMappingRegion [orientation=" + orientation + ", start=" + getStart() + ", stop=" + getStop()
				+ ", constant=" + getConstant() + "]";
	}
}
