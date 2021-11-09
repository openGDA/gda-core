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

package uk.ac.diamond.daq.mapping.region;

import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.X;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.X_START;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.X_STOP;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.Y;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.Y_START;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.Y_STOP;

import java.util.Map;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;

public class RectangularMappingRegion extends DefaultCoordinatePCSRegion implements IMappingScanRegionShape {

	private static final String NAME = "Rectangle";

    public RectangularMappingRegion() {
    	super(Map.of(X_START, 0.0, X_STOP, 1.0, Y_START, 0.0, Y_STOP, 1.0));
    }

	@Override
	public String getName() {
		return NAME;
	}

	public double getxStart() {
		return coordinates.get(X_START);
	}

	public void setxStart(double newValue) {
		updatePropertyValuesAndFire(Map.of(X_START, newValue));
	}

	public double getxStop() {
		return coordinates.get(X_STOP);
	}

	public void setxStop(double newValue) {
		updatePropertyValuesAndFire(Map.of(X_STOP, newValue));
	}

	public double getyStart() {
		return coordinates.get(Y_START);
	}

	public void setyStart(double newValue) {
		updatePropertyValuesAndFire(Map.of(Y_START, newValue));
	}

	public double getyStop() {
		return coordinates.get(Y_STOP);
	}

	public void setyStop(double newValue) {
		updatePropertyValuesAndFire(Map.of(Y_STOP, newValue));
	}

	@Override
	public void updateFromROI(IROI newROI) {
		if (newROI instanceof RectangularROI) {
			RectangularROI roi = (RectangularROI) newROI;
			if (roi.getAngle() != 0.0) {
				throw new IllegalArgumentException("Rectangular mapping region does not support angled RectangularROIs");
			}
			updatePropertyValuesAndFire(Map.of(
					X_START, roi.getPoint()[X],
					X_STOP, roi.getPoint()[X] + roi.getLengths()[X],
					Y_START, roi.getPoint()[Y],
					Y_STOP, roi.getPoint()[Y] + roi.getLengths()[Y]));
		} else {
			throw new IllegalArgumentException("Rectangular mapping region can only update from a RectangularROI");
		}
	}

	@Override
	public RectangularROI toROI() {
		RectangularROI roi = new RectangularROI();
		roi.setPoint(Math.min(getxStart(), getxStop()), Math.min(getyStart(), getyStop()));
		roi.setLengths(Math.abs(getxStop() - getxStart()), Math.abs(getyStop() - getyStart()));
		return roi;
	}

	@Override
	public String whichPlottingRegionType() {
		return RegionType.BOX.toString();
	}

	@Override
	public IMappingScanRegionShape copy() {
		final RectangularMappingRegion copy = new RectangularMappingRegion();
		copy.updatePropertyValuesAndFire(coordinates);
		return copy;
	}

	@Override
	public void centre(double x0, double y0) {
		double halfXLength = Math.abs(getxStop() - getxStart()) / 2.0;
		int xSign = getxStart() < getxStop() ? 1 : -1;
		double halfYLength = Math.abs(getyStop() - getyStart()) / 2.0;
		int ySign = getyStart() < getyStop() ? 1 : -1;
		updatePropertyValuesAndFire(Map.of(
				X_START, x0 - xSign * halfXLength,
				X_STOP, x0 + xSign * halfXLength,
				Y_START, y0 - ySign * halfYLength,
				Y_STOP, y0 + ySign * halfYLength));
	}

	@Override
	public void updateFromPropertiesMap(Map<String, Object> properties) {
		updateAndFireFromPropertiesMap(properties);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(getxStart());
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(getxStop());
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(getyStart());
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(getyStop());
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
		RectangularMappingRegion other = (RectangularMappingRegion) obj;
		if (Double.doubleToLongBits(getxStart()) != Double.doubleToLongBits(other.getxStart()))
			return false;
		if (Double.doubleToLongBits(getxStop()) != Double.doubleToLongBits(other.getxStop()))
			return false;
		if (Double.doubleToLongBits(getyStart()) != Double.doubleToLongBits(other.getyStart()))
			return false;
		if (Double.doubleToLongBits(getyStop()) != Double.doubleToLongBits(other.getyStop())) // NOSONAR for idiomatic consistency
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RectangularMappingRegion [xStart=" + getxStart() + ", xStop=" + getxStop() + ", yStart=" + getyStart() + ", yStop="
				+ getyStop() + ", name=" + NAME + "]";
	}
}
