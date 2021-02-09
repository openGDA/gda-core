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

import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.X_START;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.X_STOP;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.Y_START;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.Y_STOP;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;

public class RectangularMappingRegion implements IMappingScanRegionShape {

	private double xStart = 0;
	private double xStop = 1;
	private double yStart = 0;
	private double yStop = 1;
	private static final String NAME = "Rectangle";
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

	public double getxStart() {
		return xStart;
	}

	public void setxStart(double newValue) {
		double oldvalue = this.xStart;
		this.xStart = newValue;
		this.pcs.firePropertyChange(X_START, oldvalue, newValue);
	}

	public double getxStop() {
		return xStop;
	}

	public void setxStop(double newValue) {
		double oldvalue = this.xStop;
		this.xStop = newValue;
		this.pcs.firePropertyChange(X_STOP, oldvalue, newValue);
	}

	public double getyStart() {
		return yStart;
	}

	public void setyStart(double newValue) {
		double oldvalue = this.yStart;
		this.yStart = newValue;
		this.pcs.firePropertyChange(Y_START, oldvalue, newValue);
	}

	public double getyStop() {
		return yStop;
	}

	public void setyStop(double newValue) {
		double oldvalue = this.yStop;
		this.yStop = newValue;
		this.pcs.firePropertyChange(Y_STOP, oldvalue, newValue);
	}

	@Override
	public void updateFromROI(IROI newROI) {
		if (newROI instanceof RectangularROI) {
			RectangularROI roi = (RectangularROI) newROI;
			if (roi.getAngle() != 0.0) {
				throw new IllegalArgumentException("Rectangular mapping region does not support angled RectangularROIs");
			}
			// First save the old values
			double oldXStart = xStart;
			double oldXStop = xStop;
			double oldYStart = yStart;
			double oldYStop = yStop;
			// First update all the values not using the setters to avoid pcs events
			xStart = roi.getPoint()[0];
			xStop = roi.getPoint()[0] + roi.getLengths()[0];
			yStart = roi.getPoint()[1];
			yStop = roi.getPoint()[1] + roi.getLengths()[1];
			// Fire the events once the update is finished
			this.pcs.firePropertyChange(X_START, oldXStart, xStart);
			this.pcs.firePropertyChange(X_STOP, oldXStop, xStop);
			this.pcs.firePropertyChange(Y_START, oldYStart, yStart);
			this.pcs.firePropertyChange(Y_STOP, oldYStop, yStop);
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
		copy.setxStart(xStart);
		copy.setxStop(xStop);
		copy.setyStart(yStart);
		copy.setyStop(yStop);
		return copy;
	}

	@Override
	public void centre(double x0, double y0) {
		double halfXLength = Math.abs(xStop - xStart) / 2.0;
		int xSign = xStart < xStop ? 1 : -1;
		setxStart(x0 - xSign * halfXLength);
		setxStop(x0 + xSign * halfXLength);

		double halfYLength = Math.abs(yStop - yStart) / 2.0;
		int ySign = yStart < yStop ? 1 : -1;
		setyStart(y0 - ySign * halfYLength);
		setyStop(y0 + ySign * halfYLength);
	}

	@Override
	public void updateFromPropertiesMap(Map<String, Object> properties) {
		if (properties.containsKey(X_START)) {
			setxStart((double) properties.get(X_START));
		}
		if (properties.containsKey(X_STOP)) {
			setxStop((double) properties.get(X_STOP));
		}
		if (properties.containsKey(Y_START)) {
			setyStart((double) properties.get(Y_START));
		}
		if (properties.containsKey(Y_STOP)) {
			setyStop((double) properties.get(Y_STOP));
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		RectangularMappingRegion other = (RectangularMappingRegion) obj;
		if (Double.doubleToLongBits(xStart) != Double.doubleToLongBits(other.xStart))
			return false;
		if (Double.doubleToLongBits(xStop) != Double.doubleToLongBits(other.xStop))
			return false;
		if (Double.doubleToLongBits(yStart) != Double.doubleToLongBits(other.yStart))
			return false;
		if (Double.doubleToLongBits(yStop) != Double.doubleToLongBits(other.yStop)) // NOSONAR for idiomatic consistency
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RectangularMappingRegion [xStart=" + xStart + ", xStop=" + xStop + ", yStart=" + yStart + ", yStop="
				+ yStop + ", name=" + NAME + "]";
	}
}
