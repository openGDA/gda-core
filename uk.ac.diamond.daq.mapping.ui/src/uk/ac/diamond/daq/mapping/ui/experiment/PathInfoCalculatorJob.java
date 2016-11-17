/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment;


import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.IBoundingBoxModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;

class PathInfoCalculatorJob extends Job {

	static final String PATH_CALCULATION_TOPIC = "uk/ac/diamond/daq/mapping/client/events/PathCalculationEvent";
	static final int MAX_POINTS_IN_ROI = 100000; // 100,000

	@Inject
	private IPointGeneratorService pointGeneratorFactory;
	@Inject
	private IEventBroker eventBroker;

	private IScanPathModel scanPathModel;
	private IMappingScanRegionShape scanRegion;

	PathInfoCalculatorJob() {
		super("Calculating scan path");
		setPriority(SHORT);
	}

	void setScanPathModel(IScanPathModel scanPathModel) {
		this.scanPathModel = scanPathModel;
	}

	void setScanRegion(IMappingScanRegionShape scanRegion) {
		this.scanRegion = scanRegion;
	}

	@Override
	public IStatus run(IProgressMonitor monitor) {
		monitor.beginTask("Calculating points for scan path", IProgressMonitor.UNKNOWN);
		PathInfo pathInfo = new PathInfo();
		String xAxisName = "x";
		String yAxisName = "y";

		if (scanPathModel instanceof IBoundingBoxModel) {
			IBoundingBoxModel boxModel = (IBoundingBoxModel) scanPathModel;
			xAxisName = boxModel.getFastAxisName();
			yAxisName = boxModel.getSlowAxisName();

			if (scanRegion!=null && scanRegion.toROI() != null) {
				// We set the roi to the bounds of the first region.
				// TODO Is this correct?
				IRectangularROI roi = scanRegion.toROI().getBounds();
				if (roi!=null) {
				    BoundingBox box = boxModel.getBoundingBox();
				    if (box==null) {
				    	box = new BoundingBox();
				    	boxModel.setBoundingBox(box);
				    }
					box.setFastAxisStart(roi.getPointX());
					box.setSlowAxisStart(roi.getPointY());
					box.setFastAxisLength(roi.getLength(0));
					box.setSlowAxisLength(roi.getLength(1));
				}
			}
		}
		try {
			final Iterable<IPosition> pointIterable = pointGeneratorFactory.createGenerator(scanPathModel, scanRegion.toROI());
			double lastX = Double.NaN;
			double lastY = Double.NaN;
			for (IPosition point : pointIterable) {
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				pathInfo.pointCount++;

				if (pathInfo.pointCount > 1) {
					double thisXStep = Math.abs(point.getValue(xAxisName) - lastX);
					double thisYStep = Math.abs(point.getValue(yAxisName) - lastY);
					double thisAbsStep = Math.sqrt(Math.pow(thisXStep, 2) + Math.pow(thisYStep, 2));
					if (thisXStep > 0) {
						pathInfo.smallestXStep = Math.min(pathInfo.smallestXStep, thisXStep);
					}
					if (thisYStep > 0) {
						pathInfo.smallestYStep = Math.min(pathInfo.smallestYStep, thisYStep);
					}
					pathInfo.smallestAbsStep = Math.min(pathInfo.smallestAbsStep, thisAbsStep);
				}

				lastX = point.getValue(xAxisName);
				lastY = point.getValue(yAxisName);
				if (pathInfo.xCoordinates.size() <= MAX_POINTS_IN_ROI) {
					pathInfo.xCoordinates.add(Double.valueOf(lastX));
					pathInfo.yCoordinates.add(Double.valueOf(lastY));
				}
			}
			monitor.done();
			eventBroker.post(PATH_CALCULATION_TOPIC, pathInfo);
		} catch (Exception e) {
			return new Status(IStatus.WARNING, "uk.ac.diamond.daq.mapping.ui", "Error calculating scan path", e);
		}
		return Status.OK_STATUS;
	}
}