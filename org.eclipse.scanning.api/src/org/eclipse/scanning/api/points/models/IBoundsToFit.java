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

package org.eclipse.scanning.api.points.models;

/**
 * Interface for models that can be asked to have both their points (non-continuous motion) and bounds (continuous
 * motion, half-distance between points either side) within their region.
 * Therefore, as the number of points in an axis -> 0, the 0th point trends towards the centre of a region,
 * opposed to the non boundsFit behaviour that as number of points -> 0, 0th point remains at the start of the axial model.
 *
 * In the case of Stepped models, this reduces the number of points that can fit into each axis by one (by moving the
 * stepped region half a step in from both edges).
 *
 * In the case of Counted models, this reduces the distance between points, from Length / M to Length / (M - 1):
 * i.e. by a factor of (M - 1) / M in each axis.
 * Where M = Number of points - 1
 *
 */
public interface IBoundsToFit {

	static final String PROPERTY_NAME_BOUNDS_TO_FIT = "boundsToFit";
	static final String PROPERTY_DEFAULT_BOUNDS_FIT = "org.eclipse.mapping.boundsToFit";

	public boolean isBoundsToFit();

	public void setBoundsToFit(boolean boundsToFit);

	default void defaultBoundsToFit() {
		setBoundsToFit(Boolean.getBoolean(PROPERTY_DEFAULT_BOUNDS_FIT));
	}

	/**
	 * Returns the number of points that fit onto a line, with standard assumptions:
	 * 1. If a point is within 1% of the end of the line, it is included in the line
	 * 2a. For a model with !boundsToFit, the Nth point is at start + N * step
	 * 2b. For a model with boundsToFit, the Nth point is at start + (N + 0.5) * step
	 * 3. Except in the case that the step and length are directed in opposite directions,
	 * the 0th point will always be included, and so the minimum number of points is 1.
	 *
	 * Allows this behaviour to be consistent between models that define a step.
	 * @param length the model's length
	 * @param step the model's step length
	 * @param boundsToFit model.isBoundsToFit()
	 * @return the number of points for the underlying point generator to place in this axis
	 */
	static int getPointsOnLine(double length, double step, boolean boundsToFit) {
		double effectiveLength = Math.abs(length) + Math.abs(step) * (boundsToFit ? 0.01 : 1.01);
		// Handle 0/0 case, (any other step = 0 will be prevented by validation) and -N/0 case
		if (length == 0 || step == 0) {
			return 1;
		}
		int points = (int) Math.max(effectiveLength / Math.abs(step), 1);
		// Allow to return the correct (but invalid) negative number of steps if required.
		return (int) (points * Math.signum(step * length));
	}

	/**
	 * Returns the length of the step of a model, for a model with boundsToFit, this should
	 * be capped at the length of the model in that axis, so that its bounds remain within the region.
	 * For a model with !boundsToFit, returns the passed step.
	 * For a model with boundsToFit, returns the magnitude of whichever is larger of step and length, but facing in the direction of step.
	 * @param length the model's length
	 * @param step the model's step length
	 * @param boundsToFit model.isBoundsToFit()
	 * @return the step length for this model, to be used by generators and later static functions in this class.
	 */
	static double getLongestFittingStep(double length, double step, boolean boundsToFit) {
		if (boundsToFit && Math.abs(length) < Math.abs(step)) {
			return Math.abs(length) * Math.signum(step);
		}
		return step;
	}

	/**
	 * Returns the start point of a model, with standard assumptions:
	 * 1. The 0th point of a model with !boundsToFit should be at the start of its range
	 * However, the underlying Python generator, in the case of a single point, has special casing to
	 * place the point in the centre of the range
	 * 2. The 0th point of a model with boundsToFit should be 1/2 step in from the edges
	 * However, the same Python behaviour handles this half step, so we instead return the edge of the range.
	 *
	 * This method steps a boundsToFit model in half a step from the edge, unless it is for a single point.
	 * It steps a !boundsToFit model in half a step if it is a single point.
	 * Otherwise it returns the model's start position.
	 * @param start the model's start
	 * @param isSinglePoint	numPoints == 1
	 * @param step the step calculated from IBoundsToFit.getLongestFittingStep
	 * @param boundsToFit model.isBoundsToFit()
	 * @return the value of start to pass to the generator of this model.
	 */
	static double getFirstPoint(double start, boolean isSinglePoint, double step, boolean boundsToFit) {
		if (boundsToFit) {
			return isSinglePoint ? start : start + step/2;
		}
		return isSinglePoint ? start - step / 2 : start;
	}

	/**
	 * Returns the stop point of a model, with standard assumptions:
	 * 1. The underlying point generator takes the number of points on the line, and therefore any excess length
	 * beyond the integer number of steps must be removed.
	 * 2. The Nth point of a model with !boundsToFit should be at start + N * step
	 * However, the underlying Python generator, in the case of a single point, has special casing to
	 * place the point in the centre of the range
	 * 3. The Nth point of a model with boundsToFit should be 1/2 step in from the edges
	 * However, the same Python behaviour handles this half step, so we instead return the edge of the range.
	 *
	 * This method returns a position on the line that is an integer numbers of steps along the line from its start,
	 * either within the model's stop or 1% of the step length outside.
	 * It steps a !boundsToFit model in half a step if it is a single point.
	 * Otherwise it returns the model's start position.
	 * @param start the model's start
	 * @param numPoints the number of points calculated from IBoundsToFit.getPointsOnLine
	 * @param step the step calculated from IBoundsToFit.getLongestFittingStep
	 * @param boundsToFit model.isBoundsToFit()
	 * @return the value of stop to pass to the generator of this model.
	 */
	static double getFinalPoint(double start, int numPoints, double step, boolean boundsToFit) {
		if (boundsToFit) {
			return (numPoints == 1) ? start + step : start + (numPoints - 0.5) * step;
		}
		return (numPoints == 1) ? start + step / 2 :  start + (numPoints - 1) * step;
	}

}
