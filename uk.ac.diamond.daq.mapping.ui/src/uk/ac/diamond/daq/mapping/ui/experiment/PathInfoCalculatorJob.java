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


import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IMapPathModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;

public class PathInfoCalculatorJob extends Job {

	public static final String PATH_CALCULATION_TOPIC = "uk/ac/diamond/daq/mapping/client/events/PathCalculationEvent";
	static final int MAX_POINTS_IN_ROI = 100000; // 100,000

	private String pathCalculationTopic = PATH_CALCULATION_TOPIC;

	@Inject
	private IPointGeneratorService pointGeneratorFactory;
	@Inject
	private IEventBroker eventBroker;

	private IScanPointGeneratorModel scanPathModel;
	private IMappingScanRegionShape scanRegion;

	private Consumer<PathInfo> consumer;

	PathInfoCalculatorJob() {
		super("Calculating scan path");
		setPriority(SHORT);
		consumer = pathInfo -> eventBroker.post(pathCalculationTopic, pathInfo);
	}

	public void setPathInfoConsumer(Consumer<PathInfo> consumer) {
		this.consumer = consumer;
	}

	public void setScanPathModel(IScanPointGeneratorModel scanPathModel) {
		this.scanPathModel = scanPathModel;
	}

	public void setScanRegion(IMappingScanRegionShape scanRegion) {
		this.scanRegion = scanRegion;
	}

	@Override
	public IStatus run(IProgressMonitor monitor) {
		monitor.beginTask("Calculating points for scan path", IProgressMonitor.UNKNOWN);

		// Deduces the names of the X and Y axes from the scan model. Defaults to "x" and "y"
		// if it can't find any
		String xAxisName = "x";
		String yAxisName = "y";
		if (scanPathModel instanceof IMapPathModel) {
			IMapPathModel mapPathModel = (IMapPathModel) scanPathModel;
			xAxisName = mapPathModel.getxAxisName();
			yAxisName = mapPathModel.getyAxisName();
		}

		try {
			PathInfo pathInfo = calculatePathInfo(
					xAxisName,
					yAxisName);
			monitor.done();

			// The consumer decides how to handle the path info event
			consumer.accept(pathInfo);
		} catch (Exception e) {
			return new Status(IStatus.WARNING, "uk.ac.diamond.daq.mapping.ui", "Error calculating scan path", e);
		}
		return Status.OK_STATUS;
	}

    private PathInfo calculatePathInfo(
    		String xAxisName,
    		String yAxisName) throws Exception {
		// Invokes ScanPointGenerator to create the points
		final IPointGenerator<CompoundModel> pointGenerator = pointGeneratorFactory
				.createGenerator(scanPathModel, scanRegion.toROI());

		// Computes metadata that PathInfo requires (see PathInfo fields)
		return calculatePathInfo(
				pointGenerator,
				xAxisName,
				yAxisName);
	}

    private PathInfo calculatePathInfo(
    		Iterable<IPosition> points,
    		String xAxisName,
    		String yAxisName) {
    	// Initialise with sensible starting values
    	int pointCount = 0;
		double smallestXStep = Double.MAX_VALUE;
		double smallestYStep = Double.MAX_VALUE;
		double smallestAbsStep = Double.MAX_VALUE;
		List<Double> xCoordinates = new ArrayList<>();
		List<Double> yCoordinates = new ArrayList<>();
		double lastX = Double.NaN;
		double lastY = Double.NaN;

		// Iterates through the points, stores the x and y positions in separate
		// lists and keeps track of:
		//  - The number of points
		//  - The smallest distance between two consecutive x positions
		//  - The smallest distance between two consecutive y positions
		//  - The smallest distance between two consecutive positions in 2D space
		for (IPosition point : points) {
			pointCount++;

			if (pointCount > 1) {
				// Updates the smallest distance tracking variables if the distance
				// between this point and the last is smaller than the smallest
				// distance we've seen so far. Do this for x, y and 2D space.
				double thisXStep = Math.abs(point.getValue(xAxisName) - lastX);
				double thisYStep = Math.abs(point.getValue(yAxisName) - lastY);
				double thisAbsStep = Math.sqrt(Math.pow(thisXStep, 2) + Math.pow(thisYStep, 2));
				if (thisXStep > 0) {
					smallestXStep = Math.min(smallestXStep, thisXStep);
				}
				if (thisYStep > 0) {
					smallestYStep = Math.min(smallestYStep, thisYStep);
				}
				smallestAbsStep = Math.min(smallestAbsStep, thisAbsStep);
			}

			// Ensures no more than MAX_POINTS_IN_ROI points are inserted into
			// the PathInfo. Still need to iterate through the rest of the points
			// to accurately calculate step sizes and number of points.
			lastX = point.getValue(xAxisName);
			lastY = point.getValue(yAxisName);
			if (xCoordinates.size() <= MAX_POINTS_IN_ROI) {
				xCoordinates.add(Double.valueOf(lastX));
				yCoordinates.add(Double.valueOf(lastY));
			}
		}

		return new PathInfo(
				pointCount,
				smallestXStep,
				smallestYStep,
				smallestAbsStep,
				xCoordinates,
				yCoordinates);
    }
}