/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

import java.util.List;

import org.ejml.simple.SimpleMatrix;

/**
 * <h1>transforms between the beamline and reference frame axial systems.</h1>
 *
 * @author ton99817
 */
public class AxesTransform {
	private SimpleMatrix beamlineToReferenceTransform;

	/**
	 * Constructor method.
	 * @param beamlineToReferenceTransform a 3x3 transform matrix as a primitive double array type
	 */
	public AxesTransform(double[][] beamlineToReferenceTransform) {
		int nrows = beamlineToReferenceTransform.length;
		int ncols = nrows;

		Boolean isValid = nrows == 3? true: false;

		for (int row=0; row < nrows; row++) {
			if (beamlineToReferenceTransform[row].length != ncols) {
				isValid = false;
			}
		}

		if (isValid.equals(false)) {
			throw new IllegalArgumentException("non valid matrix given as input");
		}

		this.beamlineToReferenceTransform = new SimpleMatrix(beamlineToReferenceTransform);
	}

	/**
	 * Constructor method.
	 * @param beamlineToReferenceTransform a 3x3 transform matrix as a SimpleMatrix
	 */
	public AxesTransform(SimpleMatrix beamlineToReferenceTransform) {
		int nrows = beamlineToReferenceTransform.numRows();
		int ncols = beamlineToReferenceTransform.numCols();

		Boolean isValid = ((nrows == ncols) & ( nrows == 3))? true: false;

		if (isValid.equals(false)) {
			throw new IllegalArgumentException("non valid matrix given as input");
		}

		this.beamlineToReferenceTransform = beamlineToReferenceTransform;

	}

	/**
	 * Default constructor left for Spring configuration
	 */
	public AxesTransform() {

	}

	/**
	 * Transforms a beamline column vector to the reference axes.
	 * @param beamline The column vector as indices along the beamline axes.
	 * @return List<Double> the column vector as a list of doubles of indices along the reference axes.
	 */
	public List<List<Double>> beamlineToReferenceColumnVector(List<List<Double>> beamline) {
		if (isValidColumnVector(beamline).equals(false)) {
			throw new IllegalArgumentException("non-valid input vector");
		}

		SimpleMatrix beamlineVector = Maths.listOfListsToSimpleMatrix(beamline);
		SimpleMatrix referenceVector = beamlineToReferenceTransform.mult(beamlineVector);
		return Maths.simpleMatrixToListOfLists(referenceVector);
	}

	/**
	 * Transforms a beamline U matrix to the reference geometry.
	 * @param beamlineU The U matrix expressed in beamline axes as a nested ArrayList.
	 * @return List<List<Double>> The U matrix in reference axes as a nested ArrayList.
	 */
	public List<List<Double>> beamlineToReferenceU(List<List<Double>> beamlineU) {
		if (isValidMatrix(beamlineU).equals(false)) {
			throw new IllegalArgumentException("non-valid input matrix");
		}

		SimpleMatrix beamlineUMatrix = Maths.listOfListsToSimpleMatrix(beamlineU);
		SimpleMatrix referenceUMatrix = beamlineToReferenceTransform.mult(beamlineUMatrix.mult(beamlineToReferenceTransform.invert()));

		return Maths.simpleMatrixToListOfLists(referenceUMatrix);
	}

	/**
	 * Transforms a beamline UB matrix to the reference geometry.
	 * @param beamlineUb The UB matrix expressed in beamline axes as a nested ArrayList.
	 * @return List<List<Double>> The UB matrix in reference axes as a nested ArrayList.
	 */
	public List<List<Double>> beamlineToReferenceUb(List<List<Double>> beamlineUb) {
		if (isValidMatrix(beamlineUb).equals(false)) {
			throw new IllegalArgumentException("non-valid input matrix");
		}

		SimpleMatrix beamlineUbMatrix = Maths.listOfListsToSimpleMatrix(beamlineUb);
		SimpleMatrix referenceUbMatrix = beamlineToReferenceTransform.mult(beamlineUbMatrix);

		return Maths.simpleMatrixToListOfLists(referenceUbMatrix);
	}

	/**
	 * Transforms a column vector with indices along reference axes to the beamline axes.
	 * @param reference A list of 3 elements making up the column vector along reference axes. For example, a list [1,2,3] indicates a column vector 1i + 2j + 3k.
	 * @return List<Double> The column vector along beamline axes.
	 */
	public List<List<Double>> referenceToBeamlineColumnVector(List<List<Double>> reference) {
		if (isValidColumnVector(reference).equals(false)) {
			throw new IllegalArgumentException("non-valid input vector");
		}

		SimpleMatrix referenceVector = Maths.listOfListsToSimpleMatrix(reference);
		return referenceToBeamlineColumnVector(referenceVector);
	}

	/**
	 * Transforms a column vector with indices along reference axes to the beamline axes.
	 * @param referenceVector A SimpleMatrix of the column vector. Must be 3x1 dimensions (rows x columns).
	 * @return List<Double> The column vector along beamline axes.
	 */
	public List<List<Double>> referenceToBeamlineColumnVector(SimpleMatrix referenceVector) {
		if (isValidColumnMatrix(referenceVector).equals(false)) {
			throw new IllegalArgumentException("non-valid input vector");
		}

		SimpleMatrix beamlineVector = beamlineToReferenceTransform.invert().mult(referenceVector);
		return Maths.simpleMatrixToListOfLists(beamlineVector);
	}

	/**
	 * Transforms the U matrix from being expressed along reference axes to beamline axes.
	 * @param referenceU The 3x3 U matrix with components along reference axes.
	 * @return List<List<Double>> The U matrix expressed as components along beamline axes.
	 */
	public List<List<Double>> referenceToBeamlineU(List<List<Double>> referenceU) {
		if (isValidMatrix(referenceU).equals(false)) {
			throw new IllegalArgumentException("non-valid input matrix");
		}

		SimpleMatrix referenceUMatrix = Maths.listOfListsToSimpleMatrix(referenceU);
		SimpleMatrix beamlineUMatrix = beamlineToReferenceTransform.invert().mult(referenceUMatrix).mult(beamlineToReferenceTransform);

		return Maths.simpleMatrixToListOfLists(beamlineUMatrix);
	}

	/**
	 * Transforms the UB matrix from being expressed along reference axes to beamline axes.
	 * @param referenceUb The 3x3 UB matrix with components along reference axes.
	 * @return List<List<Double>> The UB matrix expressed as components along beamline axes.
	 */
	public List<List<Double>> referenceToBeamlineUb(List<List<Double>> referenceUb) {
		if (isValidMatrix(referenceUb).equals(false)) {
			throw new IllegalArgumentException("non-valid input matrix");
		}

		SimpleMatrix referenceUbMatrix = Maths.listOfListsToSimpleMatrix(referenceUb);
		SimpleMatrix beamlineUbMatrix = beamlineToReferenceTransform.invert().mult(referenceUbMatrix);

		return Maths.simpleMatrixToListOfLists(beamlineUbMatrix);
	}

	/**
	 * Determines if input matrix is valid.
	 * This is based on its shape; it must be a 3x3 square matrix to be valid for
	 * many methods in this class to work.
	 *
	 * @param matrix A nested ArrayList containing the matrix being checked.
	 * @returns Boolean Result of validity; either true (valid) or false (invalid).
	 */
	private Boolean isValidMatrix(List<List<Double>> matrix) {
		Integer nrows = matrix.size();

		List<Integer> rowLengths = matrix.stream().map(List::size).toList();
		float ncols = (float) rowLengths.stream().mapToInt(Integer::intValue).sum() / rowLengths.size();

		return (ncols == nrows) && (nrows == 3);
	}

	/**
	 * Determines if input column vector is valid.
	 * This is based on its shape; it must be 3x1 (rows x columns) to be valid for
	 * methods in this class to work.
	 *
	 * @param vector A nested ArrayList containing the vector being checked.
	 * @returns Boolean Result of validity; either true (valid) or false (invalid).
	 */
	private Object isValidColumnVector(List<List<Double>> vector) {
		Integer nrows = vector.size();

		List<Integer> rowLengths = vector.stream().map(List::size).toList();
		Integer ncols = rowLengths.stream().mapToInt(Integer::intValue).sum() / rowLengths.size();

		return (nrows == 3) && (ncols == 1);
	}


	/**
	 * Determines if input column matrix is valid.
	 * This is based on its shape; it must be a 3x1 (rows x columns) column matrix
	 * to be valid for many methods in this class to work.
	 *
	 * @param matrix A SimpleMatrix containing the matrix being checked.
	 * @returns Boolean Result of validity; either true (valid) or false (invalid).
	 */
	private Boolean isValidColumnMatrix(SimpleMatrix matrix) {
		Integer nrows = matrix.numRows();
		Integer ncols = matrix.numCols();

		return (ncols == 1) && (nrows == 3);
	}

	public List<List<Double>> getBeamlineToReferenceTransform() {
		return Maths.simpleMatrixToListOfLists(beamlineToReferenceTransform);
	}

	public void setBeamlineToReferenceTransform(List<List<Double>> transform) {
		this.beamlineToReferenceTransform = Maths.listOfListsToSimpleMatrix(transform);
	}

}
