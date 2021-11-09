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

import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.RADIUS;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.X;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.X_CENTRE;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.Y;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.Y_CENTRE;

import java.util.Map;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;

public class CircularMappingRegion extends DefaultCoordinatePCSRegion implements IMappingScanRegionShape {

	private static final String NAME = "Circle";

	public CircularMappingRegion() {
		super(Map.of(X_CENTRE, 0.0, Y_CENTRE, 0.0, RADIUS, 1.0));
	}

	public double getxCentre() {
		return coordinates.get(X_CENTRE);
	}

	public void setxCentre(double newValue) {
		updatePropertyValuesAndFire(Map.of(X_CENTRE, newValue));
	}

	public double getyCentre() {
		return coordinates.get(Y_CENTRE);
	}

	public void setyCentre(double newValue) {
		updatePropertyValuesAndFire(Map.of(Y_CENTRE, newValue));
	}

	public double getRadius() {
		return coordinates.get(RADIUS);
	}

	public void setRadius(double newValue) {
		updatePropertyValuesAndFire(Map.of(RADIUS, newValue));
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void updateFromROI(IROI newROI) {
		if (newROI instanceof CircularROI) {
			CircularROI roi = (CircularROI) newROI;
			updatePropertyValuesAndFire(Map.of(
					X_CENTRE, roi.getPoint()[X],
					Y_CENTRE, roi.getPoint()[Y],
					RADIUS, roi.getRadius()));
		} else {
			throw new IllegalArgumentException("Circular mapping region can only update from a CircularROI");
		}
	}

	@Override
	public CircularROI toROI() {
		CircularROI roi = new CircularROI();
		roi.setPoint(getxCentre(), getyCentre());
		roi.setRadius(getRadius());
		return roi;
	}

	@Override
	public String whichPlottingRegionType() {
		return RegionType.CIRCLE.toString();
	}

	@Override
	public IMappingScanRegionShape copy() {
		final CircularMappingRegion copy = new CircularMappingRegion();
		copy.updatePropertyValuesAndFire(coordinates);
		return copy;
	}

	@Override
	public void centre(double x0, double y0) {
		updatePropertyValuesAndFire(Map.of(
				X_CENTRE, x0,
				Y_CENTRE, y0));
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
		temp = Double.doubleToLongBits(getRadius());
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(getxCentre());
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(getyCentre());
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
		CircularMappingRegion other = (CircularMappingRegion) obj;
		if (Double.doubleToLongBits(getRadius()) != Double.doubleToLongBits(other.getRadius()))
			return false;
		if (Double.doubleToLongBits(getxCentre()) != Double.doubleToLongBits(other.getxCentre()))
			return false;
		if (Double.doubleToLongBits(getyCentre()) != Double.doubleToLongBits(other.getyCentre())) // NOSONAR for idiomatic consistency
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CircularMappingRegion [xCentre=" + getxCentre() + ", yCentre=" + getyCentre() + ", radius=" + getRadius() + "]";
	}

}
