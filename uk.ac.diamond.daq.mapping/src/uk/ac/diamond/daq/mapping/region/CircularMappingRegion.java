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
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.X_CENTRE;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.Y_CENTRE;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;

public class CircularMappingRegion implements IMappingScanRegionShape {

	private double xCentre = 0;
	private double yCentre = 0;
	private double radius = 1;
	private static final String NAME = "Circle";

	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
	}

	public double getxCentre() {
		return xCentre;
	}

	public void setxCentre(double newValue) {
		double oldvalue = this.xCentre;
		this.xCentre = newValue;
		this.pcs.firePropertyChange(X_CENTRE, oldvalue, newValue);
	}

	public double getyCentre() {
		return yCentre;
	}

	public void setyCentre(double newValue) {
		double oldvalue = this.yCentre;
		this.yCentre = newValue;
		this.pcs.firePropertyChange(Y_CENTRE, oldvalue, newValue);
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double newValue) {
		double oldvalue = this.radius;
		this.radius = newValue;
		this.pcs.firePropertyChange(RADIUS, oldvalue, newValue);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void updateFromROI(IROI newROI) {
		if (newROI instanceof CircularROI) {
			CircularROI roi = (CircularROI) newROI;
			// First save the old values
			double oldxCentre = xCentre;
			double oldyCentre = yCentre;
			double oldRadius = radius;
			// First update all the values not using the setters to avoid pcs events
			xCentre = roi.getPoint()[0];
			yCentre = roi.getPoint()[1];
			radius = roi.getRadius();
			// Fire the events once the update is finished
			this.pcs.firePropertyChange(X_CENTRE, oldxCentre, xCentre);
			this.pcs.firePropertyChange(Y_CENTRE, oldyCentre, yCentre);
			this.pcs.firePropertyChange(RADIUS, oldRadius, radius);
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
		copy.setxCentre(xCentre);
		copy.setyCentre(yCentre);
		copy.setRadius(radius);
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
		if (properties.containsKey(Y_CENTRE)) {
			setyCentre((double) properties.get(Y_CENTRE));
		}
		if (properties.containsKey(RADIUS)) {
			setRadius((double) properties.get(RADIUS));
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(radius);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(xCentre);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(yCentre);
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
		if (Double.doubleToLongBits(radius) != Double.doubleToLongBits(other.radius))
			return false;
		if (Double.doubleToLongBits(xCentre) != Double.doubleToLongBits(other.xCentre))
			return false;
		if (Double.doubleToLongBits(yCentre) != Double.doubleToLongBits(other.yCentre)) // NOSONAR for idiomatic consistency
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CircularMappingRegion [xCentre=" + xCentre + ", yCentre=" + yCentre + ", radius=" + radius + "]";
	}

}
