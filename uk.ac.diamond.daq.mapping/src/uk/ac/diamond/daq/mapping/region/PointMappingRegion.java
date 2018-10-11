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
import org.eclipse.dawnsci.analysis.dataset.roi.PointROI;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;

public class PointMappingRegion implements IMappingScanRegionShape {

	private double xPosition = 0;
	private double yPosition = 0;
	private static final String NAME = "Point";
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
	}

	public double getxPosition() {
		return xPosition;
	}

	public void setxPosition(double newValue) {
		double oldvalue = this.xPosition;
		this.xPosition = newValue;
		this.pcs.firePropertyChange("xPosition", oldvalue, newValue);
	}

	public double getyPosition() {
		return yPosition;
	}

	public void setyPosition(double newValue) {
		double oldvalue = this.yPosition;
		this.yPosition = newValue;
		this.pcs.firePropertyChange("yPosition", oldvalue, newValue);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void updateFromROI(IROI newROI) {
		if (newROI instanceof PointROI) {
			PointROI roi = (PointROI) newROI;
			// First save the old values
			double oldxPosition = xPosition;
			double oldYPosition = yPosition;
			// First update all the values not using the setters to avoid pcs events
			xPosition = roi.getPoint()[0];
			yPosition = roi.getPoint()[1];
			// Fire the events once the update is finished
			this.pcs.firePropertyChange("xPosition", oldxPosition, xPosition);
			this.pcs.firePropertyChange("yPosition", oldYPosition, yPosition);
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
		copy.setxPosition(xPosition);
		copy.setyPosition(yPosition);
		return copy;
	}

	@Override
	public void centre(double x0, double y0) {
		setxPosition(x0);
		setyPosition(y0);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(xPosition);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(yPosition);
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
		if (Double.doubleToLongBits(xPosition) != Double.doubleToLongBits(other.xPosition))
			return false;
		if (Double.doubleToLongBits(yPosition) != Double.doubleToLongBits(other.yPosition)) // NOSONAR for idiomatic consistency
			return false;
		return true;
	}

}
