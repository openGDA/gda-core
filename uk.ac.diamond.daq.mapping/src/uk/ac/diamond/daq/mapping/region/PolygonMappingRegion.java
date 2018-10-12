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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolygonalROI;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;

public class PolygonMappingRegion implements IMappingScanRegionShape {

	private static final String NAME = "Polygon";
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private final List<MutablePoint> points = new ArrayList<>();
	private final PropertyChangeListener pointListener = pcs::firePropertyChange;

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

	public List<MutablePoint> getPoints() {
		return points;
	}

	public void setPoints(List<MutablePoint> newValue) {
		points.forEach(point -> point.removePropertyChangeListener(pointListener));
		points.clear();
		points.addAll(newValue);
		points.forEach(point -> point.addPropertyChangeListener(pointListener));
		// PCS will always be fired due to null previous value
		pcs.firePropertyChange("points", null, newValue);
	}

	@Override
	public PolygonalROI toROI() {
		PolygonalROI polygonalROI = new PolygonalROI();
		for (MutablePoint point : points) {
			polygonalROI.insertPoint(point.getX(), point.getY());
		}
		return polygonalROI;
	}

	@Override
	public String whichPlottingRegionType() {
		return RegionType.POLYGON.toString();
	}

	@Override
	public void updateFromROI(IROI newROI) {
		if (newROI instanceof PolygonalROI) {
			PolygonalROI roi = (PolygonalROI) newROI;
			List<MutablePoint> newPoints = new ArrayList<>();
			for (int i = 0; i < roi.getNumberOfPoints(); i++) {
				newPoints.add(new MutablePoint(roi.getPointX(i), roi.getPointY(i)));
			}
			setPoints(newPoints);
		} else {
			throw new IllegalArgumentException("Polygon mapping region can only update from a PolygonalROI");
		}
	}

	public static class MutablePoint {

		private double x;
		private double y;

		private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

		public void addPropertyChangeListener(PropertyChangeListener listener) {
			pcs.addPropertyChangeListener(listener);
		}

		public void removePropertyChangeListener(PropertyChangeListener listener) {
			pcs.removePropertyChangeListener(listener);
		}

		public MutablePoint() {
			x = 0;
			y = 0;
		}

		public MutablePoint(double x, double y) {
			this.x = x;
			this.y = y;
		}

		public double getX() {
			return x;
		}

		public void setX(double newValue) {
			double oldvalue = x;
			x = newValue;
			pcs.firePropertyChange("x", oldvalue, newValue);
		}

		public double getY() {
			return y;
		}

		public void setY(double newValue) {
			double oldValue = y;
			y = newValue;
			pcs.firePropertyChange("y", oldValue, newValue);
		}

		@Override
		public String toString() {
			return "MutablePoint["+x+", "+y+"]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(x);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(y);
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
			MutablePoint other = (MutablePoint) obj;
			if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
				return false;
			return (Double.doubleToLongBits(y) == Double.doubleToLongBits(other.y));
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + points.hashCode();
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
		PolygonMappingRegion other = (PolygonMappingRegion) obj;
		return points.equals(other.points);
	}

}
