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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.scanning.api.annotation.UiComesAfter;
import org.eclipse.scanning.api.annotation.UiHidden;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;

public class CircularMappingRegion implements IMappingScanRegionShape {

	private double xCenter = 0;
	private double yCenter = 0;
	private double radius = 1;
	private String name = "Circle";
	private final RegionType regionType = RegionType.CIRCLE;

	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
	}

	@UiComesAfter("regionShape")
	public double getxCenter() {
		return xCenter;
	}

	public void setxCenter(double newValue) {
		double oldvalue = this.xCenter;
		this.xCenter = newValue;
		this.pcs.firePropertyChange("xCenter", oldvalue, newValue);
	}

	@UiComesAfter("xCenter")
	public double getyCenter() {
		return yCenter;
	}

	public void setyCenter(double newValue) {
		double oldvalue = this.yCenter;
		this.yCenter = newValue;
		this.pcs.firePropertyChange("yCenter", oldvalue, newValue);
	}

	@UiComesAfter("yCenter")
	public double getRadius() {
		return radius;
	}

	public void setRadius(double newValue) {
		double oldvalue = this.radius;
		this.radius = newValue;
		this.pcs.firePropertyChange("radius", oldvalue, newValue);
	}

	@Override
	@UiHidden
	public String getName() {
		return name;
	}

	@Override
	public void updateFromROI(IROI newROI) throws IllegalArgumentException {
		if (newROI instanceof CircularROI) {
			CircularROI roi = (CircularROI) newROI;
			// First save the old values
			double oldXCenter = xCenter;
			double oldYCenter = yCenter;
			double oldRadius = radius;
			// First update all the values not using the setters to avoid pcs events
			xCenter = roi.getPoint()[0];
			yCenter = roi.getPoint()[1];
			radius = roi.getRadius();
			// Fire the events once the update is finished
			this.pcs.firePropertyChange("xCenter", oldXCenter, xCenter);
			this.pcs.firePropertyChange("yCenter", oldYCenter, yCenter);
			this.pcs.firePropertyChange("radius", oldRadius, radius);
		} else {
			throw new IllegalArgumentException("Circular mapping region can only update from a CircularROI");
		}
	}

	@Override
	public CircularROI toROI() {
		CircularROI roi = new CircularROI();
		roi.setPoint(getxCenter(), getyCenter());
		roi.setRadius(getRadius());
		return roi;
	}

	@Override
	public String whichPlottingRegionType() {
		return regionType.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(radius);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		temp = Double.doubleToLongBits(xCenter);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(yCenter);
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
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (Double.doubleToLongBits(xCenter) != Double.doubleToLongBits(other.xCenter))
			return false;
		if (Double.doubleToLongBits(yCenter) != Double.doubleToLongBits(other.yCenter))
			return false;
		return true;
	}
}
