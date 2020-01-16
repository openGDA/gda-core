/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package org.eclipse.scanning.points;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.EllipticalROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PointROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolygonalROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.SectorROI;
import org.eclipse.scanning.api.points.models.ScanRegion;
import org.python.core.PyObject;

public class ROIGenerator {

	public static final PyObject[] EMPTY_PY_ARRAY = new PyObject[0];

	private ROIGenerator() {
	}

	private static Map<Class<?>, Function<IROI, PyObject>> roiDispatchMap;

	static {
		roiDispatchMap = new HashMap<>();

		roiDispatchMap.put(CircularROI.class, r -> ScanPointGeneratorFactory.JCircularROIFactory().createObject(
				((CircularROI) r).getCentre(), ((CircularROI) r).getRadius()));
		roiDispatchMap.put(EllipticalROI.class, r -> ScanPointGeneratorFactory.JEllipticalROIFactory().createObject(
				((EllipticalROI) r).getPoint(), ((EllipticalROI) r).getSemiAxes(), ((EllipticalROI) r).getAngle()));
		roiDispatchMap.put(LinearROI.class, r -> null); // not supported
		roiDispatchMap.put(PointROI.class, r -> ScanPointGeneratorFactory.JPointROIFactory().createObject(
				((PointROI) r).getPoint()));
		roiDispatchMap.put(PolygonalROI.class, r -> {
			PolygonalROI p = (PolygonalROI) r;
			double[] xPoints = new double[p.getNumberOfPoints()];
			double[] yPoints = new double[p.getNumberOfPoints()];
			for (int i = 0; i < xPoints.length; i++) {
				PointROI point = p.getPoint(i);
				xPoints[i] = point.getPointX();
				yPoints[i] = point.getPointY();
			}
			return ScanPointGeneratorFactory.JPolygonalROIFactory().createObject(xPoints, yPoints);
		});
		roiDispatchMap.put(RectangularROI.class, r -> ScanPointGeneratorFactory.JRectangularROIFactory().createObject(
				((RectangularROI) r).getPoint(), ((RectangularROI) r).getLength(0), ((RectangularROI) r).getLength(1),
				((RectangularROI) r).getAngle()));
		roiDispatchMap.put(SectorROI.class, r -> ScanPointGeneratorFactory.JSectorROIFactory().createObject(
				((SectorROI) r).getPoint(), ((SectorROI) r).getRadii(), ((SectorROI) r).getAngles()));
	}

	protected static PyObject makePyRoi(Object region) {
		IROI roi = null;
		if (region instanceof ScanRegion) {
			region = ((ScanRegion) region).getRoi();
		}
		if (region instanceof IROI) {
			roi = (IROI) region;
		} else {
			return null;
		}
		if (roiDispatchMap.containsKey(roi.getClass())) {
			return roiDispatchMap.get(roi.getClass()).apply(roi);
		} else {
			return null;
		}
	}

}
