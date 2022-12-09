/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.path;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IAxialModel;
import org.eclipse.scanning.api.points.models.IMapPathModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.IPathInfoCalculator;
import uk.ac.diamond.daq.mapping.api.PathInfoCalculationException;
import uk.ac.diamond.daq.mapping.api.document.scanpath.PathInfo;
import uk.ac.diamond.daq.mapping.api.document.scanpath.PathInfoRequest;

/**
 * Implementation of {@link IPathInfoCalculator} that uses {@link IPointGeneratorService}
 * to generate scan points and produce a {@link PathInfo} object
 */
public class PointGeneratorPathInfoCalculator implements IPathInfoCalculator<PathInfoRequest> {
	private static final Logger logger = LoggerFactory.getLogger(PointGeneratorPathInfoCalculator.class);

	private static final String DEFAULT_X_AXIS_NAME = "x";
	private static final String DEFAULT_Y_AXIS_NAME = "y";

	@Override
	public PathInfo calculatePathInfo(PathInfoRequest request) throws PathInfoCalculationException {
		IScanPointGeneratorModel scanPathModel = request.getScanPathModel();
		try {
			// Invokes ScanPointGenerator to create the points
			IPointGenerator<CompoundModel> pointGenerator = getPointGeneratorService()
					.createGenerator(scanPathModel, request.getScanRegion());

			// Initialise with sensible starting values
	    	int pointCount = 0;
			double smallestXStep = Double.MAX_VALUE;
			double smallestYStep = Double.MAX_VALUE;
			double smallestAbsStep = Double.MAX_VALUE;
			List<Double> xCoordinates = new ArrayList<>();
			List<Double> yCoordinates = new ArrayList<>();
			double lastX = Double.NaN;
			double lastY = Double.NaN;

			String xAxisName = getXAxisName(scanPathModel);
			String yAxisName = getYAxisName(scanPathModel);

			// Iterates through the points, stores the x and y positions in separate
			// lists and keeps track of:
			//  - The number of points
			//  - The smallest distance between two consecutive x positions
			//  - The smallest distance between two consecutive y positions
			//  - The smallest distance between two consecutive positions in 2D space
			for (IPosition point : pointGenerator) {
				pointCount++;

				if (pointCount > 1) {
					// Updates the smallest distance tracking variables if the distance
					// between this point and the last is smaller than the smallest
					// distance we've seen so far. Do this for x, y and 2D space.
					double thisXStep = Math.abs(point.getDouble(xAxisName) - lastX);
					double thisYStep = Math.abs(point.getDouble(yAxisName) - lastY);
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
				lastX = point.getDouble(xAxisName);
				lastY = point.getDouble(yAxisName);
				if (xCoordinates.size() < request.getMaxPoints()) {
					xCoordinates.add(Double.valueOf(lastX));
					yCoordinates.add(Double.valueOf(lastY));
				}
			}

			// outerAxisMultiplier is the product of the number of points
			// in each outer axis. It defaults to 1 if there are no outer axes.
			// It is multiplied by the number of points in the mapping scan
			// e.g. if the mapping scan in x and y has 25 points and is
			// also projected back through 10 points in z then there are
			// 250 points in total.
			int outerAxisMultiplier = calculateAllOuterPoints(request.getOuterScannables());
			int totalPoints = outerAxisMultiplier * pointCount;

			return PathInfo.builder()
					.withEventId(request.getEventId())
					.withSourceId(request.getSourceId())
					.withInnerPointCount(pointCount)
					.withTotalPointCount(totalPoints)
					.withSmallestXStep(smallestXStep)
					.withSmallestYStep(smallestYStep)
					.withSmallestAbsStep(smallestAbsStep)
					.withxCoordinateList(xCoordinates)
					.withyCoordinateList(yCoordinates)
					.build();
		} catch (GeneratorException e) {
			throw new PathInfoCalculationException(e);
		}
	}

	private String getXAxisName(IScanPointGeneratorModel scanPathModel) {
		return asMapPathModel(scanPathModel)
				.map(IMapPathModel::getxAxisName)
				.orElse(DEFAULT_X_AXIS_NAME);
	}

	private String getYAxisName(IScanPointGeneratorModel scanPathModel) {
		return asMapPathModel(scanPathModel)
				.map(IMapPathModel::getyAxisName)
				.orElse(DEFAULT_Y_AXIS_NAME);
	}

	private Optional<IMapPathModel> asMapPathModel(IScanPointGeneratorModel scanPathModel) {
		if (scanPathModel instanceof IMapPathModel)
			return Optional.of((IMapPathModel) scanPathModel);
		else
			return Optional.empty();
	}

	private int calculateAllOuterPoints(List<IAxialModel> outerPaths) {
		// Multiplies together the number of points in each outer scan path
		return outerPaths.stream()
				.map(this::calculateOuterPoints)
				.reduce(1, (product, next) -> product * next);
	}

    private int calculateOuterPoints(IScanPathModel outerPath) {
		try {
			IPointGenerator<?> generator = getPointGeneratorService().createGenerator(outerPath);
			return generator.size();
		} catch (GeneratorException e) {
			logger.error("Could not get size of outer path '{}'", outerPath.getName(), e);
			return 1;
		}
    }

	private IPointGeneratorService getPointGeneratorService() {
		return PlatformUI.getWorkbench().getService(IPointGeneratorService.class);
	}
}
