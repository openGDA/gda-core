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
import org.eclipse.scanning.api.annotation.UiHidden;
import org.eclipse.scanning.api.points.Point;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;

public class PolygonMappingRegion implements IMappingScanRegionShape {

	private String name = "Polygon";
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private List<Point> points = new ArrayList<>(10);

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
	}

	@Override
	@UiHidden
	public String getName() {
		return name;
	}

	public List<Point> getPoints() {
		return points;
	}

	public void setPoints(List<Point> newValue) {
		List<Point> oldvalue = this.points;
		this.points = newValue;
		this.pcs.firePropertyChange("points", oldvalue, newValue);
	}

	@Override
	public PolygonalROI toROI() {
		PolygonalROI polygonalROI = new PolygonalROI();
		for (Point point : points) {
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
			// Rebuild the list of points
			points.clear();
			for (int i = 0; i < roi.getNumberOfPoints(); i++) {
				points.add(new Point(i, roi.getPointX(i), i, roi.getPointY(i)));
			}
			// PCS will always be fired
			this.pcs.firePropertyChange("points", null, points);
		} else {
			throw new IllegalArgumentException("Polygon mapping region can only update from a PolygonalROI");
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((points == null) ? 0 : points.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		if (points == null) {
			if (other.points != null)
				return false;
		} else if (!points.equals(other.points))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
