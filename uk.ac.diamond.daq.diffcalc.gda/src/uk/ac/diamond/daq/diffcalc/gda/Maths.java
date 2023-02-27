/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.diffcalc.gda;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ejml.simple.SimpleMatrix;

public class Maths {
	/*
	 * Inspired by: https://www.techiedelight.com/convert-list-of-lists-to-2d-array-java/
	 */
	public static SimpleMatrix listOfListsToSimpleMatrix(List<List<Double>> nestedList) {
		double[][] arr = nestedList.stream()
				.map(l -> l.stream().mapToDouble(Double::doubleValue).toArray())
				.toArray(double[][]::new);


		return new SimpleMatrix(arr);
	}

	public static SimpleMatrix listToColumnVector(List<Double> list) {
		List<List<Double>> listOfLists = Arrays.asList(list);

		return listOfListsToSimpleMatrix(listOfLists).transpose();

	}

	public static List<List<Double>> columnVectorFromCoordsList(List<Double> coords) {
		List<List<Double>> result = new ArrayList<>();

		for (Double coord: coords) {
			result.add(Arrays.asList(coord));
		}

		return result;
	}

	public static List<Double> coordsListFromColumnVector(List<List<Double>> columnVector) {
		if (!validColumnVector(columnVector)) {
			throw new IllegalArgumentException("input needs to be a column vector, i.e. only one column in matrix");
		}

		List<Double> result = new ArrayList<>();

		for (List<Double> row: columnVector) {
			result.add(row.get(0));
		}

		return result;
	}

	/*
	 * Assumes input matrices are column vectors.
	 */
	public static SimpleMatrix crossProduct(SimpleMatrix a, SimpleMatrix b) {
		Double a1 = a.get(0,0);
		Double a2 = a.get(1,0);
		Double a3 = a.get(2,0);

		Double b1 = b.get(0,0);
		Double b2 = b.get(1,0);
		Double b3 = b.get(2,0);
		return new SimpleMatrix(
				new double[][] {
					new double[] { a2*b3 - a3*b2 },
					new double[] { a3*b1 - a1*b3 },
					new double[] { a1*b2 - a2*b1 }
				}
		);
	}

	public static List<Double> columnVectorToList(SimpleMatrix vector) {
		if (vector.numCols() != 1) {
			throw new IllegalArgumentException("input needs to be a column vector, i.e. only one column in matrix");
		}

		List<List<Double>> listOfLists = simpleMatrixToListOfLists(vector.transpose());
		return listOfLists.get(0);
	}

	public static List<List<Double>> simpleMatrixToListOfLists(SimpleMatrix matrix) {

		List<List<Double>> resultingList = new ArrayList<>();

		for (int i = 0; i< matrix.numRows(); i++) {
			List<Double> row = new ArrayList<>();

			for (int j = 0; j< matrix.numCols(); j++) {
				row.add(matrix.get(i, j));
			}
			resultingList.add(row);

		}

		return resultingList;
	}

	private static boolean validColumnVector(List<List<Double>> columnVector) {
		double sumNumberOfRows = columnVector.stream().map(List::size).mapToDouble(f->f).sum();
		return (sumNumberOfRows % 1) == 0;
	}

	/**
	 * Returns difference between two angles.
	 *
	 * Not the same as a regular difference, as angle values repeat every 2pi (or 360 if in degrees).
	 * @param a first angle
	 * @param b second angle
	 * @return the difference between these angles if they were between -pi and pi.
	 */
	public static Double angleDifference(Double a, Double b) {
		return Math.toDegrees(
				2.0 * Math.asin(
						Math.abs(Math.sin(Math.toRadians(a - b) / 2.0))
						)
				);
	}

}
