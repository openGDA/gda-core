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

import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.X_CENTRE;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.X_RANGE;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.Y_CENTRE;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.Y_RANGE;

import java.util.Map;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;

public class CentredRectangleMappingRegion extends DefaultCoordinatePCSRegion implements IMappingScanRegionShape {

	private static final String NAME = "Centred Rectangle";

    public CentredRectangleMappingRegion() {
    	super(Map.of(X_CENTRE, 0.5, X_RANGE, 1.0, Y_CENTRE, 0.5, Y_RANGE, 1.0));
    }

	@Override
	public String getName() {
		return NAME;
	}

	public double getxCentre() {
		return coordinates.get(X_CENTRE);
	}

	public void setxCentre(double newValue) {
		updatePropertyValuesAndFire(Map.of(X_CENTRE, newValue));
	}

	public double getxRange() {
		return coordinates.get(X_RANGE);
	}

	public void setxRange(double newValue) {
		updatePropertyValuesAndFire(Map.of(X_RANGE, newValue));
	}

	public double getyCentre() {
		return coordinates.get(Y_CENTRE);
	}

	public void setyCentre(double newValue) {
		updatePropertyValuesAndFire(Map.of(Y_CENTRE, newValue));
	}

	public double getyRange() {
		return coordinates.get(Y_RANGE);
	}

	public void setyRange(double newValue) {
		updatePropertyValuesAndFire(Map.of(Y_RANGE, newValue));
	}

	@Override
	public void updateFromROI(IROI newROI) {
		if (!(newROI instanceof RectangularROI)) {
			throw new IllegalArgumentException("Centre rectangle mapping region can only update from a Rectangular ROI");
		}

		RectangularROI roi = (RectangularROI) newROI;
		if (roi.getAngle() != 0.0) {
			throw new IllegalArgumentException("Centre rectangle mapping region does not support angled RectangularROIs");
		}
		double[] topLeft = roi.getPoint();
		double[] lengths = roi.getLengths();
		updatePropertyValuesAndFire(Map.of(
				X_CENTRE, topLeft[0] + lengths[0] / 2,
				X_RANGE, lengths[0],
				Y_CENTRE, topLeft[1] + lengths[1] / 2,
				Y_RANGE, lengths[1]));
	}

	@Override
	public IROI toROI() {
		RectangularROI roi = new RectangularROI();
		roi.setPoint(getxCentre() - getxRange() / 2, getyCentre() - getyRange() / 2);
		roi.setLengths(getxRange(), getyRange());
		return roi;
	}

	@Override
	public String whichPlottingRegionType() {
		return RegionType.BOX.toString();
	}

	@Override
	public IMappingScanRegionShape copy() {
		final CentredRectangleMappingRegion copy = new CentredRectangleMappingRegion();
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
		temp = Double.doubleToLongBits(getxCentre());
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(getxRange());
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(getyCentre());
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(getyRange());
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
		CentredRectangleMappingRegion other = (CentredRectangleMappingRegion) obj;
		if (Double.doubleToLongBits(getxCentre()) != Double.doubleToLongBits(other.getxCentre()))
			return false;
		if (Double.doubleToLongBits(getxRange()) != Double.doubleToLongBits(other.getxRange()))
			return false;
		if (Double.doubleToLongBits(getyCentre()) != Double.doubleToLongBits(other.getyCentre()))
			return false;
		if (Double.doubleToLongBits(getyRange()) != Double.doubleToLongBits(other.getyRange())) // NOSONAR for idiomatic consistency
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CentredRectangleMappingRegion [xCentre=" + getxCentre() + ", xRange=" + getxRange() + ", yCentre=" + getyCentre()
				+ ", yRange=" + getyRange() + "]";
	}

}
