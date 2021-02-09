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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;

public class CentredRectangleMappingRegion implements IMappingScanRegionShape {

	private double xCentre = 0.5;
	private double xRange = 1.0;
	private double yCentre = 0.5;
	private double yRange = 1.0;
	private static final String NAME = "Centred Rectangle";
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
	}

	@Override
	public String getName() {
		return NAME;
	}

	public double getxCentre() {
		return xCentre;
	}

	public void setxCentre(double newValue) {
		double oldValue = this.xCentre;
		this.xCentre = newValue;
		this.pcs.firePropertyChange(X_CENTRE, oldValue, newValue);
	}

	public double getxRange() {
		return xRange;
	}

	public void setxRange(double newValue) {
		double oldValue = this.xRange;
		this.xRange = newValue;
		this.pcs.firePropertyChange(X_RANGE, oldValue, newValue);
	}

	public double getyCentre() {
		return yCentre;
	}

	public void setyCentre(double newValue) {
		double oldValue = this.yCentre;
		this.yCentre = newValue;
		this.pcs.firePropertyChange(Y_CENTRE, oldValue, newValue);
	}

	public double getyRange() {
		return yRange;
	}

	public void setyRange(double newValue) {
		double oldValue = this.yRange;
		this.yRange = newValue;
		this.pcs.firePropertyChange(Y_RANGE, oldValue, newValue);
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

		// First save the old values
		double oldXCentre = xCentre;
		double oldXRange = xRange;
		double oldYCentre = yCentre;
		double oldYRange = yRange;

		// Update all the values not using the setter to avoid pcs events
		double[] topLeft = roi.getPoint();
		double[] lengths = roi.getLengths();
		xRange = lengths[0];
		xCentre = topLeft[0] + xRange / 2;
		yRange = lengths[1];
		yCentre = topLeft[1] + yRange / 2;

		// Fire the events now that the update is finished
		this.pcs.firePropertyChange(X_CENTRE, oldXCentre, xCentre);
		this.pcs.firePropertyChange(X_RANGE, oldXRange, xRange);
		this.pcs.firePropertyChange(Y_CENTRE, oldYCentre, yCentre);
		this.pcs.firePropertyChange(Y_RANGE, oldYRange, yRange);
	}

	@Override
	public IROI toROI() {
		RectangularROI roi = new RectangularROI();
		roi.setPoint(xCentre - xRange / 2, yCentre - yRange / 2);
		roi.setLengths(xRange, yRange);

		return roi;
	}

	@Override
	public String whichPlottingRegionType() {
		return RegionType.BOX.toString();
	}

	@Override
	public IMappingScanRegionShape copy() {
		final CentredRectangleMappingRegion copy = new CentredRectangleMappingRegion();
		copy.setxCentre(xCentre);
		copy.setxRange(xRange);
		copy.setyCentre(yCentre);
		copy.setyRange(yRange);
		return copy;
	}

	@Override
	public void centre(double x0, double y0) {
		setxCentre(x0);
		setyCentre(y0);
	}

	@Override
	public void updateFromPropertiesMap(Map<String, Object> properties) {
		if (properties.containsKey(X_CENTRE)) {
			setxCentre((double) properties.get(X_CENTRE));
		}
		if (properties.containsKey(X_RANGE)) {
			setxRange((double) properties.get(X_RANGE));
		}
		if (properties.containsKey(Y_CENTRE)) {
			setyCentre((double) properties.get(Y_CENTRE));
		}
		if (properties.containsKey(Y_RANGE)) {
			setyRange((double) properties.get(Y_RANGE));
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(xCentre);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(xRange);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(yCentre);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(yRange);
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
		if (Double.doubleToLongBits(xCentre) != Double.doubleToLongBits(other.xCentre))
			return false;
		if (Double.doubleToLongBits(xRange) != Double.doubleToLongBits(other.xRange))
			return false;
		if (Double.doubleToLongBits(yCentre) != Double.doubleToLongBits(other.yCentre))
			return false;
		if (Double.doubleToLongBits(yRange) != Double.doubleToLongBits(other.yRange)) // NOSONAR for idiomatic consistency
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CentredRectangleMappingRegion [xCentre=" + xCentre + ", xRange=" + xRange + ", yCentre=" + yCentre
				+ ", yRange=" + yRange + "]";
	}

}
