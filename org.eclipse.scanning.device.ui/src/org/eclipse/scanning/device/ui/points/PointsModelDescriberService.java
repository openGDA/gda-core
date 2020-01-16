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

package org.eclipse.scanning.device.ui.points;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scanning.api.points.models.AxialArrayModel;
import org.eclipse.scanning.api.points.models.AxialCollatedStepModel;
import org.eclipse.scanning.api.points.models.AxialMultiStepModel;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.JythonGeneratorModel;
import org.eclipse.scanning.api.points.models.StaticModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsRandomOffsetModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridStepModel;
import org.eclipse.scanning.api.points.models.TwoAxisLinePointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisLineStepModel;
import org.eclipse.scanning.api.points.models.TwoAxisLissajousModel;
import org.eclipse.scanning.api.points.models.TwoAxisPointSingleModel;
import org.eclipse.scanning.api.points.models.TwoAxisPtychographyModel;
import org.eclipse.scanning.api.points.models.TwoAxisSpiralModel;

public class PointsModelDescriberService implements IPointsModelDescriberService {

	private Map<Class<? extends IScanPathModel>, PointsModelDescriber> descs;
	private static String contAlte = "\nThe scan supports continuous Malcolm operation and alternating mode [when wrapped in an outer scan].";
	private static String stepIcon = "icons/scanner--step.png";
	private static String gridIcon = "icons/scanner--grid.png";
	private static String lineIcon = "icons/scanner--line.png";

	static {
		System.out.println("Starting ModelDescriber service");
		Map<Class<? extends IScanPathModel>, PointsModelDescriber> descs = new HashMap<>();
		descs.put(AxialStepModel.class, new PointsModelDescriber("Axial Step Scan",
				"Creates a scan that steps through a Scannable axis, from start to the highest multiple of the step lower than the stop."
				+ "\nIf the last requested point is within 1% of the end it will still be included in the scan."
				+ contAlte, stepIcon, true));
		descs.put(AxialCollatedStepModel.class, new PointsModelDescriber("Collated Axial Step Scan",
				"Creates a scan that steps through multiple Scannable axes simultaneously, from start to the highest multiple of the step lower than the stop."
				+ "\nIf the last requested point is within 1% of the end it will still be included in the scan."
				+ contAlte, stepIcon, false));
		descs.put(AxialMultiStepModel.class, new PointsModelDescriber("Axial MultiStep Scan",
				"Creates a scan that steps through a Scannable axes in multiple Step scans:"
				+ "\nFor each from start to the highest multiple of the step lower than the stop."
				+ "\nIf the first point of a scan is within 1% of the end of the previous scan and the previous scanned finished there, the point is removed."
				+ contAlte, stepIcon, true));
		descs.put(AxialArrayModel.class, new PointsModelDescriber("Axial Array Scan",
				"Creates a scan from an array of positions for a single Scannable axis."
				+ contAlte, null, true));
		descs.put(TwoAxisGridPointsModel.class, new PointsModelDescriber("Two Axis Grid Points Scan",
				"Creates a grid scan in two Scannable axes by slicing each axis into equal sized sections."
				+ contAlte, gridIcon, true));
		descs.put(TwoAxisGridPointsRandomOffsetModel.class, new PointsModelDescriber("Random Offset Two Axis Grid Points Scan",
				"Creates a grid scan in two Scannable axes by slicing each axis into equal sized sections."
				+ "\nEach position is then offset proportional to the step in the fast axis."
				+ contAlte, gridIcon, true));
		descs.put(TwoAxisGridStepModel.class, new PointsModelDescriber("Two Axis Grid Step Scan",
				"Creates a grid scan in two Scannable axes by stepping through each axis, from start to the highest multiple of the step lower than the length."
				+ contAlte, gridIcon, true));
		descs.put(TwoAxisLinePointsModel.class, new PointsModelDescriber("Two Axis Line Points Scan",
				"Creates a line scan in two Scannable axes by slicing the line into equal sized sections."
				+ contAlte, lineIcon, true));
		descs.put(TwoAxisLineStepModel.class, new PointsModelDescriber("Two Axis Line Step Scan",
				"Creates a line scan in two Scannable axes by stepping along the line, from start to the highest multiple of the step lower than the length."
				+ contAlte, lineIcon, true));
		descs.put(StaticModel.class, new PointsModelDescriber("Static/Acquire/Empty Scan",
				"Empty generator used when wrapping malcolm scans with no CPU steps.",
				null, false));
		descs.put(TwoAxisSpiralModel.class, new PointsModelDescriber("Two Axis Spiral Scan",
				"Creates a spiral scan in two Scannable axes by stepping around the center of a bounding region."
				+ "\nThis is an Archimedean spiral with polar form: r=b*theta."
				+ "The 'Scale' parameter gives approximately both the distance between arcs and the arclength between consecutive points."
				+ contAlte,	"icons/scanner--spiral.png", true));
		descs.put(TwoAxisLissajousModel.class, new PointsModelDescriber("Two-Axis Lissajous Curve Scan",
				"Creates a Lissajous curve scan inside a bounding box, with points placed evenly in parametric t."
				+ "\na/b is floored as the number of lobes, and phase difference is 0 for even lobes, pi/2 for odd."
				+ contAlte,	"icons/scanner--lissajous.png", true));
		descs.put(JythonGeneratorModel.class, new PointsModelDescriber("Jython Model Scan",
				"Uses a Python function to get the motor positions for the scan.",	"icons/scanner--function.png", true));
		descs.put(TwoAxisPtychographyModel.class, new PointsModelDescriber("Ptychography / Random Offset Two Axis Grid Step Scan",
				"Creates a grid scan in two Scannable axes by stepping through each axis, from start to the highest multiple of the step lower than the length."
				+"\nWhere the step is proportional to allow overlap with the previous point, modeling the beam as a rectangle in the scannable axes."
				+ "\nEach position is then offset proportional to the step in its axis."
				+ contAlte, gridIcon, true));
		descs.put(TwoAxisPointSingleModel.class, new PointsModelDescriber("Two-Axis Point Scan",
				"Creates a scan in two Scannable axes by placing a single point at a co-ordinate in those axes.",
				"icons/scanner--plus.png", true));
	}

	@Override
	public String getIconPath(Class<? extends IScanPathModel> clazz) {
		return getModelDescriber(clazz).getIconPath();
	}

	@Override
	public String getDescription(Class<? extends IScanPathModel> clazz) {
		return getModelDescriber(clazz).getDescription();
	}

	@Override
	public String getLabel(Class<? extends IScanPathModel> clazz) {
		return getModelDescriber(clazz).getLabel();
	}

	@Override
	public PointsModelDescriber getModelDescriber(Class<? extends IScanPathModel> clazz) {
		return descs.get(clazz);
	}

}
