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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;

import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Test;

import uk.ac.diamond.daq.diffcalc.gda.AxesTransform;
import uk.ac.diamond.daq.diffcalc.gda.Maths;

/**
 * <h1>Test class to check that transforms between beamline/reference axes are reasonable.</h1>
 */
class AxesTransformTest {

	SimpleMatrix identity = new SimpleMatrix(
		new double[][] {
			new double[] {1d, 0d, 0d},
			new double[] {0d, 1d, 0d},
			new double[] {0d, 0d, 1d},
		}
	);
	SimpleMatrix reverse = new SimpleMatrix(
		new double[][] {
			new double[] {0d, 0d, 1d},
			new double[] {0d, 1d, 0d},
			new double[] {1d, 0d, 0d},
		}
	);

	AxesTransform identityTransform = new AxesTransform(identity);
	AxesTransform reverseTransform = new AxesTransform(reverse);

	@Test
	void testBeamlineToReferenceColumnVectorMapping() {

		List<Double> beamlineCoords = Arrays.asList(1.0, 2.0, 3.0);
		List<List<Double>> beamlineVector = Maths.columnVectorFromCoordsList(beamlineCoords);

		List<List<Double>> referenceVectorIdentity = identityTransform.beamlineToReferenceColumnVector(beamlineVector);
		List<List<Double>> referenceVectorReversed = reverseTransform.beamlineToReferenceColumnVector(beamlineVector);

		assertEquals(beamlineVector, referenceVectorIdentity);

		List<Double> referenceCoordsReversed = Maths.coordsListFromColumnVector(referenceVectorReversed);

		assertEquals(beamlineCoords.get(0), referenceCoordsReversed.get(2));
		assertEquals(beamlineCoords.get(1), referenceCoordsReversed.get(1));
		assertEquals(beamlineCoords.get(2), referenceCoordsReversed.get(0));
	}

	@Test
	void testBeamlineToReferenceU() {
		List<List<Double>> uMatrix = Arrays.asList(
				Arrays.asList(1d, 2d, 3d),
				Arrays.asList(4d, 5d, 6d),
				Arrays.asList(7d, 8d, 9d)
		);

		List<List<Double>> expectedReferenceUMatrixReversed = Arrays.asList(
				Arrays.asList(9d, 8d, 7d),
				Arrays.asList(6d, 5d, 4d),
				Arrays.asList(3d, 2d, 1d)
		);

		List<List<Double>> referenceUMatrixReversed = reverseTransform.beamlineToReferenceU(uMatrix);
		List<List<Double>> referenceUMatrixIdentity = identityTransform.beamlineToReferenceU(uMatrix);

		assertEquals(referenceUMatrixReversed, expectedReferenceUMatrixReversed);
		assertEquals(referenceUMatrixIdentity, uMatrix);
	}

	@Test
	void testBeamlineToReferenceUb() {
		List<List<Double>> ubMatrix = Arrays.asList(
				Arrays.asList(1d, 2d, 3d),
				Arrays.asList(4d, 5d, 6d),
				Arrays.asList(7d, 8d, 9d)
		);

		List<List<Double>> expectedReferenceUbMatrixReversed = Arrays.asList(
				Arrays.asList(7d, 8d, 9d),
				Arrays.asList(4d, 5d, 6d),
				Arrays.asList(1d, 2d, 3d)
		);

		List<List<Double>> referenceUbMatrixReversed = reverseTransform.beamlineToReferenceUb(ubMatrix);
		List<List<Double>> referenceUbMatrixIdentity = identityTransform.beamlineToReferenceUb(ubMatrix);

		assertEquals(referenceUbMatrixReversed, expectedReferenceUbMatrixReversed);
		assertEquals(referenceUbMatrixIdentity, ubMatrix);
	}

	@Test
	void testReferenceToBeamlineColumnVectorMapping() {
		List<Double> referenceCoords = Arrays.asList(3.0, 2.0, 1.0);
		List<List<Double>> referenceVector = Maths.columnVectorFromCoordsList(referenceCoords);

		List<List<Double>> beamlineVectorIdentity = identityTransform.referenceToBeamlineColumnVector(referenceVector);
		List<List<Double>> beamlineVectorReversed = reverseTransform.referenceToBeamlineColumnVector(referenceVector);

		assertEquals(beamlineVectorIdentity, referenceVector);

		List<Double> beamlineCoordsReversed = Maths.coordsListFromColumnVector(beamlineVectorReversed);

		assertEquals(referenceCoords.get(0), beamlineCoordsReversed.get(2));
		assertEquals(referenceCoords.get(1), beamlineCoordsReversed.get(1));
		assertEquals(referenceCoords.get(2), beamlineCoordsReversed.get(0));
	}

	@Test
	void testReferenceToBeamlineU() {
		List<List<Double>> referenceU = Arrays.asList(
				Arrays.asList(9d, 8d, 7d),
				Arrays.asList(6d, 5d, 4d),
				Arrays.asList(3d, 2d, 1d)
		);

		List<List<Double>> expectedBeamlineUMatrixReversed = Arrays.asList(
				Arrays.asList(1d, 2d, 3d),
				Arrays.asList(4d, 5d, 6d),
				Arrays.asList(7d, 8d, 9d)
		);

		List<List<Double>> beamlineUMatrixReversed = reverseTransform.referenceToBeamlineU(referenceU);
		List<List<Double>> beamlineUMatrixIdentity = identityTransform.referenceToBeamlineU(referenceU);

		assertEquals(beamlineUMatrixReversed, expectedBeamlineUMatrixReversed);
		assertEquals(beamlineUMatrixIdentity, referenceU);
	}

	@Test
	void testReferenceToBeamlineUb() {
		List<List<Double>> referenceUb = Arrays.asList(
				Arrays.asList(7d, 8d, 9d),
				Arrays.asList(4d, 5d, 6d),
				Arrays.asList(1d, 2d, 3d)
		);

		List<List<Double>> expectedBeamlineUbMatrixReversed = Arrays.asList(
				Arrays.asList(1d, 2d, 3d),
				Arrays.asList(4d, 5d, 6d),
				Arrays.asList(7d, 8d, 9d)
		);

		List<List<Double>> beamlineUbMatrixReversed = reverseTransform.referenceToBeamlineUb(referenceUb);
		List<List<Double>> beamlineUbMatrixIdentity = identityTransform.referenceToBeamlineUb(referenceUb);

		assertEquals(beamlineUbMatrixReversed, expectedBeamlineUbMatrixReversed);
		assertEquals(beamlineUbMatrixIdentity, referenceUb);
	}

	@Test
	void testConversionBetweenColumnVectorsForReverseTransform() {
		List<Double> beamlineCoords = Arrays.asList(1.0, 2.0, 3.0);
		List<List<Double>> beamlineVector = Maths.columnVectorFromCoordsList(beamlineCoords);

		List<List<Double>> referenceVector = reverseTransform.beamlineToReferenceColumnVector(beamlineVector);
		List<List<Double>> recreatedBeamlineVector = reverseTransform.referenceToBeamlineColumnVector(referenceVector);

		assertEquals(recreatedBeamlineVector, beamlineVector);
	}

	@Test
	void testConversionBetweenUMatricesForReverseTransform() {
		List<List<Double>> beamlineU = Arrays.asList(
				Arrays.asList(1d, 2d, 3d),
				Arrays.asList(4d, 5d, 6d),
				Arrays.asList(7d, 8d, 9d)
		);

		List<List<Double>> referenceU = reverseTransform.beamlineToReferenceU(beamlineU);
		List<List<Double>> recreatedBeamlineU = reverseTransform.referenceToBeamlineU(referenceU);

		assertEquals(recreatedBeamlineU, beamlineU);
	}

	@Test
	void testConversionBetweenUbMatricesForReverseTransform() {
		List<List<Double>> beamlineUb = Arrays.asList(
				Arrays.asList(1d, 2d, 3d),
				Arrays.asList(4d, 5d, 6d),
				Arrays.asList(7d, 8d, 9d)
		);

		List<List<Double>> referenceUb = reverseTransform.beamlineToReferenceUb(beamlineUb);
		List<List<Double>> recreatedBeamlineUb = reverseTransform.referenceToBeamlineUb(referenceUb);

		assertEquals(recreatedBeamlineUb, beamlineUb);
	}

	@Test
	void testConversionBetweenColumnVectorsForIdentityTransform() {
		List<Double> beamlineCoords = Arrays.asList(1.0, 2.0, 3.0);
		List<List<Double>> beamlineVector = Maths.columnVectorFromCoordsList(beamlineCoords);

		List<List<Double>> referenceVector = identityTransform.beamlineToReferenceColumnVector(beamlineVector);
		List<List<Double>> recreatedBeamlineVector = identityTransform.referenceToBeamlineColumnVector(referenceVector);

		assertEquals(recreatedBeamlineVector, beamlineVector);
		assertEquals(recreatedBeamlineVector, referenceVector);
	}

	@Test
	void testConversionBetweenUMatricesForIdentityTransform() {
		List<List<Double>> beamlineU = Arrays.asList(
				Arrays.asList(1d, 2d, 3d),
				Arrays.asList(4d, 5d, 6d),
				Arrays.asList(7d, 8d, 9d)
		);

		List<List<Double>> referenceU = identityTransform.beamlineToReferenceU(beamlineU);
		List<List<Double>> recreatedBeamlineU = identityTransform.referenceToBeamlineU(referenceU);

		assertEquals(recreatedBeamlineU, beamlineU);
		assertEquals(recreatedBeamlineU, referenceU);
	}

	@Test
	void testConversionBetweenUbMatricesForIdentityTransform() {
		List<List<Double>> beamlineUb = Arrays.asList(
				Arrays.asList(1d, 2d, 3d),
				Arrays.asList(4d, 5d, 6d),
				Arrays.asList(7d, 8d, 9d)
		);

		List<List<Double>> referenceUb = identityTransform.beamlineToReferenceUb(beamlineUb);
		List<List<Double>> recreatedBeamlineUb = identityTransform.referenceToBeamlineUb(referenceUb);

		assertEquals(recreatedBeamlineUb, beamlineUb);
		assertEquals(recreatedBeamlineUb, referenceUb);
	}

	@Test
	void testExceptionsThrownForTransformMethods() {
		List<Double> badCoords = Arrays.asList(1.0, 2.0);

		List<List<Double>> badColumnVector = Maths.columnVectorFromCoordsList(badCoords);
		List<List<Double>> badU = Arrays.asList(Arrays.asList(1.0, 2.0, 3.0), Arrays.asList(4.0, 5.0, 6.0));
		List<List<Double>> badUb = Arrays.asList(Arrays.asList(1.0, 2.0, 3.0), Arrays.asList(4.0, 5.0, 6.0), Arrays.asList(7.0, 8.0));

		assertThrows(IllegalArgumentException.class, () -> reverseTransform.beamlineToReferenceColumnVector(badColumnVector));
		assertThrows(IllegalArgumentException.class, () -> reverseTransform.beamlineToReferenceU(badU));
		assertThrows(IllegalArgumentException.class, () -> reverseTransform.beamlineToReferenceUb(badUb));

	}
}
