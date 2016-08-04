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
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Point;
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
		try {
			final Iterable<IPosition> pointIterable = pointGeneratorFactory.createGenerator(scanPathModel, scanRegion.toROI());
			double lastX = Double.NaN;
			double lastY = Double.NaN;
			for (IPosition iPosition : pointIterable) {
				Point point = (Point) iPosition;
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				pathInfo.pointCount++;

				if (pathInfo.pointCount > 1) {
					double thisXStep = Math.abs(point.getX() - lastX);
					double thisYStep = Math.abs(point.getY() - lastY);
					double thisAbsStep = Math.sqrt(Math.pow(thisXStep, 2) + Math.pow(thisYStep, 2));
					if (thisXStep > 0) {
						pathInfo.smallestXStep = Math.min(pathInfo.smallestXStep, thisXStep);
					}
					if (thisYStep > 0) {
						pathInfo.smallestYStep = Math.min(pathInfo.smallestYStep, thisYStep);
					}
					pathInfo.smallestAbsStep = Math.min(pathInfo.smallestAbsStep, thisAbsStep);
				}

				lastX = point.getX();
				lastY = point.getY();
				if (pathInfo.points.size() <= MAX_POINTS_IN_ROI) {
					pathInfo.points.add(point);
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