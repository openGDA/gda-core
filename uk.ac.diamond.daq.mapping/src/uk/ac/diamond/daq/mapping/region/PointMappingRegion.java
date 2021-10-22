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
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.X_POSITION;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.Y;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.Y_POSITION;

import java.util.Map;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PointROI;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;

public class PointMappingRegion extends DefaultCoordinatePCSRegion implements IMappingScanRegionShape {

	private static final String NAME = "Point";

	public PointMappingRegion() {
		super(Map.of(X_POSITION, 0.0, Y_POSITION, 0.0));
	}

	public double getxPosition() {
		return coordinates.get(X_POSITION);
	}

	public void setxPosition(double newValue) {
		updatePropertyValuesAndFire(Map.of(X_POSITION, newValue));
	}

	public double getyPosition() {
		return coordinates.get(Y_POSITION);
	}

	public void setyPosition(double newValue) {
		updatePropertyValuesAndFire(Map.of(Y_POSITION, newValue));
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void updateFromROI(IROI newROI) {
		if (newROI instanceof PointROI) {
			PointROI roi = (PointROI) newROI;
			updatePropertyValuesAndFire(Map.of(
					X_POSITION, roi.getPoint()[X],
					Y_POSITION, roi.getPoint()[Y]));
		} else {
			throw new IllegalArgumentException("Point mapping region can only update from a PointROI");
		}
	}

	@Override
	public PointROI toROI() {
		PointROI roi = new PointROI();
		roi.setPoint(getxPosition(), getyPosition());
		return roi;
	}

	@Override
	public String whichPlottingRegionType() {
		return RegionType.POINT.toString();
	}

	@Override
	public IMappingScanRegionShape copy() {
		final PointMappingRegion copy = new PointMappingRegion();
		copy.updatePropertyValuesAndFire(coordinates);
		return copy;
	}

	@Override
	public void centre(double x0, double y0) {
		updatePropertyValuesAndFire(Map.of(
				X_POSITION, x0,
				Y_POSITION, y0));
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
		temp = Double.doubleToLongBits(getxPosition());
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(getyPosition());
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
		PointMappingRegion other = (PointMappingRegion) obj;
		if (Double.doubleToLongBits(getxPosition()) != Double.doubleToLongBits(other.getxPosition()))
			return false;
		if (Double.doubleToLongBits(getyPosition()) != Double.doubleToLongBits(other.getyPosition())) // NOSONAR for idiomatic consistency
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PointMappingRegion [xPosition=" + getxPosition() + ", yPosition=" + getyPosition() + "]";
	}

}
