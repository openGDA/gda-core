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
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.scanning.api.annotation.UiComesAfter;
import org.eclipse.scanning.api.annotation.UiHidden;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;

public class LineMappingRegion implements IMappingScanRegionShape {

	private double xStart = 0;
	private double yStart = 0;
	private double xStop = 1;
	private double yStop = 1;
	private String name = "Line";
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
	}

	public double getxStart() {
		return xStart;
	}

	public void setxStart(double newValue) {
		double oldvalue = this.xStart;
		this.xStart = newValue;
		this.pcs.firePropertyChange("xStart", oldvalue, newValue);
	}

	public double getyStart() {
		return yStart;
	}

	public void setyStart(double newValue) {
		double oldvalue = this.yStart;
		this.yStart = newValue;
		this.pcs.firePropertyChange("yStart", oldvalue, newValue);
	}

	@UiComesAfter("yStart")
	public double getxStop() {
		return xStop;
	}

	public void setxStop(double newValue) {
		double oldvalue = this.xStop;
		this.xStop = newValue;
		this.pcs.firePropertyChange("xStop", oldvalue, newValue);
	}

	public double getyStop() {
		return yStop;
	}

	public void setyStop(double newValue) {
		double oldvalue = this.yStop;
		this.yStop = newValue;
		this.pcs.firePropertyChange("yStop", oldvalue, newValue);
	}

	@Override
	@UiHidden
	public String getName() {
		return name;
	}

	@Override
	public void updateFromROI(IROI newROI) throws IllegalArgumentException {
		if (newROI instanceof LinearROI) {
			LinearROI roi = (LinearROI) newROI;
			// First save the old values
			double oldXStart = xStart;
			double oldXStop = xStop;
			double oldYStart = yStart;
			double oldYStop = yStop;
			// First update all the values not using the setters to avoid pcs events
			xStart = roi.getPoint()[0];
			xStop = roi.getEndPoint()[0];
			yStart = roi.getPoint()[1];
			yStop = roi.getEndPoint()[1];
			// Fire the events once the update is finished
			this.pcs.firePropertyChange("xStart", oldXStart, xStart);
			this.pcs.firePropertyChange("xStop", oldXStop, xStop);
			this.pcs.firePropertyChange("yStart", oldYStart, yStart);
			this.pcs.firePropertyChange("yStop", oldYStop, yStop);
		} else {
			throw new IllegalArgumentException("Line mapping region can only update from a LinearROI");
		}
	}

	@Override
	public LinearROI toROI() {
		LinearROI roi = new LinearROI();
		roi.setPoint(getxStart(), getyStart());
		roi.setEndPoint(getxStop(), getyStop());
		return roi;
	}

	@Override
	public String whichPlottingRegionType() {
		return RegionType.LINE.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		long temp;
		temp = Double.doubleToLongBits(xStart);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(xStop);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(yStart);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(yStop);
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
		LineMappingRegion other = (LineMappingRegion) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (Double.doubleToLongBits(xStart) != Double.doubleToLongBits(other.xStart))
			return false;
		if (Double.doubleToLongBits(xStop) != Double.doubleToLongBits(other.xStop))
			return false;
		if (Double.doubleToLongBits(yStart) != Double.doubleToLongBits(other.yStart))
			return false;
		if (Double.doubleToLongBits(yStop) != Double.doubleToLongBits(other.yStop))
			return false;
		return true;
	}
}
