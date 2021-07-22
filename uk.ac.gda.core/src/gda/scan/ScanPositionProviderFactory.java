/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.scan;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.python.core.PyNone;
import org.python.core.PySequence;
import org.python.core.PyTuple;

import gda.device.scannable.ScannableUtils;

/**
 *
 */
public class ScanPositionProviderFactory {

	private static class ListScanPositionProvider<T> implements ScanPositionProvider {

		private final List<T> points;

		public ListScanPositionProvider(List<T> points) {
			this.points = Collections.synchronizedList(new ArrayList<T>(points));
		}

		@Override
		public T get(int index) {
			return points.get(index);
		}

		@Override
		public int size() {
			return points.size();
		}

		@Override
		public String toString() {
			return "ScanPositionProviderFromList [points=" + points + "]";
		}

	}

	private static List<Object> calculatePoints(Object start, Object stop, Object step) {
		// ensure step is in the right direction
		final Object rStep = ScanBase.sortArguments(start, stop, step);

		// calculate the number of points:
		final int numberOfPoints = calculateNumberOfPoints(start, stop, step);

		// fill the scan points
		if (numberOfPoints != 0) {
			return Stream.iterate(start, prev -> ScannableUtils.calculateNextPoint(prev, rStep)).limit(numberOfPoints)
					.collect(toList());
		}

		return List.of(start);
	}

	private static int calculateNumberOfPoints(Object start, Object stop, Object step) {
		if (stop == null || step == null) {
			return 0;
		}

		try {
			final int numFields = getLength(start);
			if (numFields != getLength(stop) || numFields != getLength(step)) {
				return 0;
			}

			return getNumberSteps(start, stop, step, numFields) + 1;
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * Assuming the objects can be converted into doubles, this calculates the number of steps for the given InputLength
	 *
	 * @return int
	 * @throws Exception
	 */
	public static int getNumberSteps(Object start, Object stop, Object step, int parameterSize) throws Exception {
		// TODO can this method be merged with ScannableUtils.getNumberOfSteps?

		// the expected size of the start, stop and step objects
		final int numArgs = parameterSize;

		// add a small amount to values to ensure that the final point in the scan is included
		double fudgeFactor = 1e-10;

		// if there is a mismatch to the position object and the Scannable, throw an error
		if (numArgs == 1 && (start.getClass().isArray() || start instanceof PySequence)) {
			throw new Exception(
					"Position arguments do not match size of Pseudo Device. Check size of inputNames and outputFormat arrays for this object.");
		}

		// if position objects are a single value, or if no inputNames
		if (numArgs <= 1) {

			int maxSteps = 0;
			double startValue = Double.valueOf(start.toString()).doubleValue();
			double stopValue = Double.valueOf(stop.toString()).doubleValue();
			double stepValue = Math.abs(Double.valueOf(step.toString()).doubleValue());
			if (stepValue == 0) {
				throw new Exception("step size is zero so number of points cannot be calculated");
			}
			double fudgeValue = stepValue * fudgeFactor;
			double difference = Math.abs(stopValue - startValue);
			maxSteps = (int) Math.abs((difference + Math.abs(fudgeValue)) / stepValue);
			return maxSteps;
		}

		// ELSE position objects are an array
		int maxSteps = 0;
		int minSteps = java.lang.Integer.MAX_VALUE;
		// Loop through each field
		for (int i = 0; i < numArgs; i++) {
			Double startValue = getDouble(start, i);
			Double stopValue = getDouble(stop, i);
			Double stepValue = getDouble(step, i);
			if (stepValue == null) {
				if (startValue == null && stopValue == null) {
					// stepSize is null, but this is okay with no start/stop values
					continue;
				}
				throw new Exception(
						"a step field is None/null despite there being a corresponding start and/or stop value.");
			}

			if (startValue == null || stopValue == null) {
				throw new Exception("a start or end field is None/null without a corresponding None/null step size.");
			}

			double difference = Math.abs(stopValue - startValue);
			if (stepValue == 0.) {
				if (difference < fudgeFactor) {
					// zero step value okay as there is no distance to move
					continue;
				}
				throw new Exception("a step field is zero despite there being a distance to move in that direction.");
			}

			double fudgeValue = stepValue * fudgeFactor;
			int steps = (int) Math.abs((difference + Math.abs(fudgeValue)) / stepValue);
			if (steps > maxSteps) {
				maxSteps = steps;
			}
			if (steps < minSteps) {
				minSteps = steps;
			}
		}

		if (maxSteps - minSteps > 1) {
			throw new Exception("The step-vector does not connect the start and end points within the allowed\n"
					+ "tolerance of one step: in one basis direction " + maxSteps + " steps are required, but\n"
					+ "in another only " + minSteps + " steps are required.");
		}

		return minSteps;

	}

	@SuppressWarnings("rawtypes")
	private static Double getDouble(Object val, int index) {
		if (val instanceof Number[]) {
			return ((Number[]) val)[index].doubleValue();
		}
		if (val.getClass().isArray()) {
			return Array.getDouble(val, index);
		}
		if (val instanceof PySequence) {
			if (((PySequence) val).__finditem__(index) instanceof PyNone) {
				return null;
			}
			return Double.parseDouble(((PySequence) val).__finditem__(index).toString());
		}
		if (val instanceof List) {
			return Double.parseDouble(((List) val).get(index).toString());
		}
		throw new IllegalArgumentException("getDouble. Object cannot be converted to Double");
	}

	private static int getLength(Object val) {
		int len = 0;
		if (val instanceof Number[]) {
			len = ((Number[]) val).length;
		}
		if (val.getClass().isArray()) {
			len = Array.getLength(val);
		}
		if (val instanceof PySequence) {
			len = ((PySequence) val).__len__();
		}
		if (val instanceof List) {
			len = ((List<?>) val).size();
		}
		return len;
	}

	/**
	 * @param pointsList
	 * @return ScanPositionProvider
	 */
	public static <T> ScanPositionProvider create(List<T> pointsList) {
		return new ListScanPositionProvider<>(pointsList);
	}

	/**
	 * @param <T>
	 *            type of positions
	 * @param points
	 * @return ScanPositionProvider e.g. ScanPositionProviderFactory.create(new Double[]{0., 1., 2., 3.,4.,5.});
	 */
	public static <T> ScanPositionProvider create(T[] points) {
		return new ListScanPositionProvider<>(Arrays.asList(points));
	}

	/**
	 * Creates a {@link ScanPositionProvider} from a list of lists. This method is called from python code (where the
	 * lists are {@link PyTuple}s, so it should not be removed.
	 *
	 * @param regionsList list of regions - each inner list is start, stop, step
	 * @return ScanPositionProvider
	 */
	public static ScanPositionProvider createFromRegion(List<List<Object>> regionsList) {
		// TODO: DAQ-3626 merge this logic with that for ImplicitScanRegion to reduce duplication
		final List<Object> points = regionsList.stream()
				.map(l -> calculatePoints(l.get(0), l.get(1), l.get(2)))
				.collect(toList());
		return new ListScanPositionProvider<Object>(points);
	}

}
